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
import org.springframework.boot.autoconfigure.amqp.RabbitProperties.SimpleContainer;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import com.zakiis.rabbitmq.demo.domain.constant.TimedTaskConstants;

@Configuration
public class TimedTaskAmqpConfiguration {

	@Bean
	public CachingConnectionFactory timedTaskConnectionFactory(ResourceLoader resourceLoader
			, ObjectProvider<ConnectionFactoryCustomizer> connectionFactoryCustomizers) throws Exception {
		RabbitProperties rabbitProperties = new RabbitProperties();
		rabbitProperties.setAddresses("192.168.137.104:5672,192.168.137.105:5672,192.168.137.106:5672");
		rabbitProperties.setVirtualHost("timed-task");
		rabbitProperties.setUsername("timed-task");
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
	
	@Bean
	SimpleRabbitListenerContainerFactory timedTaskRabbitListenerContainerFactory(
			ConnectionFactory timedTaskConnectionFactory,
			ObjectProvider<ContainerCustomizer<SimpleMessageListenerContainer>> simpleContainerCustomizer) {
		RabbitProperties rabbitProperties = new RabbitProperties();
		SimpleContainer simple = rabbitProperties.getListener().getSimple();
		simple.setConcurrency(2);
		simple.setMaxConcurrency(5);
		simple.setAcknowledgeMode(AcknowledgeMode.AUTO);
		simple.setDefaultRequeueRejected(false);
		simple.getRetry().setEnabled(true);
		simple.getRetry().setMaxAttempts(2);
		SimpleRabbitListenerContainerFactoryConfigurer configurer = new SimpleRabbitListenerContainerFactoryConfigurer(rabbitProperties);
		
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, timedTaskConnectionFactory);
		simpleContainerCustomizer.ifUnique(factory::setContainerCustomizer);
		return factory;
	}
	
	@Bean
	public RabbitTemplate timedTaskRabbitTemplate(RabbitTemplateConfigurer configurer, ConnectionFactory timedTaskConnectionFactory) {
		RabbitTemplate template = new RabbitTemplate();
		configurer.configure(template, timedTaskConnectionFactory);
		return template;
	}
	
	@Bean
	public AmqpAdmin timedTaskAmqpAdmin(ConnectionFactory timedTaskConnectionFactory) {
		RabbitAdmin amqpAdmin = new RabbitAdmin(timedTaskConnectionFactory);
		return amqpAdmin;
	}
	
	@Bean
	public Declarables timedTaskDeclarables(AmqpAdmin timedTaskAmqpAdmin) {
		Exchange timedTaskExchange = timedTaskExchange();
		Queue rcmsQueue = rcmsQueue();
		Declarables declarables = new Declarables(timedTaskExchange
				, rcmsQueue
				, rcmsBinding(rcmsQueue, timedTaskExchange));
		for (Declarable declarable : declarables.getDeclarables()) {
			declarable.setAdminsThatShouldDeclare(timedTaskAmqpAdmin);
		}
		return declarables;
	}
	
	Exchange timedTaskExchange() {
		return ExchangeBuilder.directExchange(TimedTaskConstants.EXCHANGE_TIMED_TASK)
				.build();
	}
	
	Queue rcmsQueue() {
		return QueueBuilder.durable(TimedTaskConstants.QUEUE_RCMS)
				.maxLength(TimedTaskConstants.MAX_LENGTH_RCMS)
				.overflow(Overflow.dropHead)
				.build();
	}
	
	Binding rcmsBinding(Queue rcmsQueue, Exchange timedTaskExchange) {
		return BindingBuilder.bind(rcmsQueue)
				.to(timedTaskExchange)
				.with(TimedTaskConstants.ROUTING_KEY_RCMS)
				.noargs();
	}
}
