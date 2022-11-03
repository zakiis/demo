package com.zakiis.rabbitmq.demo.boot.autoconfigure;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueBuilder.Overflow;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.CachingConnectionFactoryConfigurer;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryCustomizer;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionFactoryBeanConfigurer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties.SimpleContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import com.zakiis.rabbitmq.demo.domain.constant.NotificationConstants;


@Configuration
public class NotificationAmqpConfiguration {
	
	@Bean
	public CachingConnectionFactory notificationConnectionFactory(ResourceLoader resourceLoader
			, ObjectProvider<ConnectionFactoryCustomizer> connectionFactoryCustomizers) throws Exception {
		RabbitProperties rabbitProperties = new RabbitProperties();
		rabbitProperties.setAddresses("192.168.137.104:5672,192.168.137.105:5672,192.168.137.106:5672");
		rabbitProperties.setVirtualHost("rcms");
		rabbitProperties.setUsername("rcms");
		rabbitProperties.setPassword("123456");
		
		RabbitConnectionFactoryBean connectionFactoryBean = new RabbitConnectionFactoryBean();
		RabbitConnectionFactoryBeanConfigurer connectionFactoryBeanConfigurer = 
				new RabbitConnectionFactoryBeanConfigurer(resourceLoader, rabbitProperties);
		connectionFactoryBeanConfigurer.configure(connectionFactoryBean);
		connectionFactoryBean.afterPropertiesSet();
		com.rabbitmq.client.ConnectionFactory connectionFactory = connectionFactoryBean.getObject();
		connectionFactoryCustomizers.orderedStream()
				.forEach((customizer) -> customizer.customize(connectionFactory));

		CachingConnectionFactoryConfigurer cachingConnectionFactoryConfigurer =
				new CachingConnectionFactoryConfigurer(rabbitProperties);
		CachingConnectionFactory factory = new CachingConnectionFactory(connectionFactory);
		cachingConnectionFactoryConfigurer.configure(factory);
		
		return factory;
	}
	
	@Bean(name = {"rabbitListenerContainerFactory", "notificationRabbitListenerContainerFactory"})
	SimpleRabbitListenerContainerFactory notificationRabbitListenerContainerFactory(
			ConnectionFactory notificationConnectionFactory,
			ObjectProvider<ContainerCustomizer<SimpleMessageListenerContainer>> simpleContainerCustomizer) {
		RabbitProperties rabbitProperties = new RabbitProperties();
		SimpleContainer simple = rabbitProperties.getListener().getSimple();
		simple.setConcurrency(2);
		simple.setMaxConcurrency(5);
		simple.setAcknowledgeMode(AcknowledgeMode.AUTO);
		simple.setDefaultRequeueRejected(true);
		simple.getRetry().setEnabled(false);
		SimpleRabbitListenerContainerFactoryConfigurer configurer = new SimpleRabbitListenerContainerFactoryConfigurer(rabbitProperties);
		
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, notificationConnectionFactory);
		simpleContainerCustomizer.ifUnique(factory::setContainerCustomizer);
		return factory;
	}
	
	@Bean
	public RabbitTemplate notificationRabbitTemplate(RabbitTemplateConfigurer configurer, ConnectionFactory notificationConnectionFactory) {
		RabbitTemplate template = new RabbitTemplate();
		configurer.configure(template, notificationConnectionFactory);
		return template;
	}
	
	@Bean
	public AmqpAdmin notificationAmqpAdmin(ConnectionFactory notificationConnectionFactory) {
		RabbitAdmin amqpAdmin = new RabbitAdmin(notificationConnectionFactory);
		return amqpAdmin;
	}
	
	@Bean
	public Declarables notificationDeclarables(AmqpAdmin notificationAmqpAdmin) {
		Exchange notificationExchange = notificationExchange();
		Queue pushQueue = pushQueue();
		Queue smsQueue = smsQueue();
		Declarables declarables = new Declarables(notificationExchange,
				pushQueue,
				smsQueue,
				pushBinding(pushQueue, notificationExchange),
				smsBinding(smsQueue, notificationExchange))
				;
		for (Declarable declarable : declarables.getDeclarables()) {
			declarable.setAdminsThatShouldDeclare(notificationAmqpAdmin);
		}
		return declarables;
	}
	
	@Bean
	public Declarables notificationDlxDeclarables(AmqpAdmin notificationAmqpAdmin) {
		Exchange notificationDlxExchange = notificationDlxExchange();
		Queue pushDlxQueue = pushDlxQueue();
		Queue smsDlxQueue = smsDlxQueue();
		Declarables declarables = new Declarables(notificationDlxExchange,
				pushDlxQueue,
				smsDlxQueue,
				pushDlxBinding(pushDlxQueue, notificationDlxExchange),
				smsDlxBinding(smsDlxQueue, notificationDlxExchange))
				;
		for (Declarable declarable : declarables.getDeclarables()) {
			declarable.setAdminsThatShouldDeclare(notificationAmqpAdmin);
		}
		return declarables;
	}
	
	Exchange notificationExchange() {
		return ExchangeBuilder.directExchange(NotificationConstants.EXCHANGE_NOTIFICATION)
				.build();
	}
	
	Exchange notificationDlxExchange() {
		return ExchangeBuilder.directExchange(NotificationConstants.EXCHANGE_NOTIFICATION_DLX)
				.build();
	}
	
	Queue pushQueue() {
		return QueueBuilder.durable(NotificationConstants.QUEUE_PUSH)
				.quorum()
				.deliveryLimit(NotificationConstants.DELIVERY_LIMIT)
				.deadLetterExchange(NotificationConstants.EXCHANGE_NOTIFICATION_DLX)
				.maxLength(NotificationConstants.MAX_LENGTH_PUSH_QUEUE)
				.overflow(Overflow.rejectPublish)
				.build();
	}
	
	Queue pushDlxQueue() {
		return QueueBuilder.durable(NotificationConstants.QUEUE_PUSH_DLX)
				.build();
	}
	
	Queue smsQueue() {
		return QueueBuilder.durable(NotificationConstants.QUEUE_SMS)
				.quorum()
				.deliveryLimit(NotificationConstants.DELIVERY_LIMIT)
				.deadLetterExchange(NotificationConstants.EXCHANGE_NOTIFICATION_DLX)
				.maxLength(NotificationConstants.MAX_LENGTH_SMS_QUEUE)
				.overflow(Overflow.rejectPublish)
				.build();
	}
	
	Queue smsDlxQueue() {
		return QueueBuilder.durable(NotificationConstants.QUEUE_SMS_DLX)
				.build();
	}
	
	Binding smsBinding(Queue smsQueue, Exchange notificationExchange) {
		return BindingBuilder.bind(smsQueue)
				.to(notificationExchange)
				.with(NotificationConstants.ROUTING_KEY_SMS)
				.noargs();
	}
	
	Binding smsDlxBinding(Queue smsDlxQueue, Exchange notificationDlxExchange) {
		return BindingBuilder.bind(smsDlxQueue)
				.to(notificationDlxExchange)
				.with(NotificationConstants.ROUTING_KEY_SMS)
				.noargs();
	}
	
	Binding pushBinding(Queue pushQueue, Exchange notificationExchange) {
		return BindingBuilder.bind(pushQueue)
				.to(notificationExchange)
				.with(NotificationConstants.ROUTING_KEY_PUSH)
				.noargs();
	}
	
	Binding pushDlxBinding(Queue pushDlxQueue, Exchange notificationDlxExchange) {
		return BindingBuilder.bind(pushDlxQueue)
				.to(notificationDlxExchange)
				.with(NotificationConstants.ROUTING_KEY_PUSH)
				.noargs();
	}
	
}
