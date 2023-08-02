package com.springBatch.spring.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.springBatch.spring.batch.entity.Customer;
import com.springBatch.spring.batch.repository.CustomerRepository;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchProcessing
//GENERATE A CONSTRUCTOR WITH 3 PARAMETERS CREATED IN THIS CLASS
@AllArgsConstructor
public class SpringBatchConfig {
	
	//SPRING BATCH CONFIGURATION V5.2

	//DEPENDENCIES INJECTION
	//THIS IS A JOB BATCH INTERFACE
	//private JobBuilderFactory jobBuilderFactory;
	
	private JobRepository jobRepository;
	private PlatformTransactionManager transactionManager;
	//THIS IS A STEP BATCH INTERFACE
	//private StepBuilderFactory stepBuilderFactory;
	
	//THIS IS OUR ENTITY
	private CustomerRepository customerRepository;


	//CREATE THE ItemReader FOLLOWING NEXT ARCHITECHTURE:
	//1. ItemReader, 2. ItemProcessor, 3. ItemWritter
	
	//------------------------------1. ItemReader-------------------------------------
	@Bean
	public FlatFileItemReader<Customer> reader(){
	FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
	itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
	itemReader.setName("csvReader");
	itemReader.setLinesToSkip(1);
	//MAP DATA FROM CSV FILE
	itemReader.setLineMapper(lineMapper());
	
	return itemReader;
	}
	
	
	//METHOD TO MAP THE CSV FILE
	private LineMapper<Customer> lineMapper(){
			DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>(); 
			
			DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
			//READ THE DATA INTO CSV FILE AND KNOW WHAT IS AN ATTRIBUTE SEPARATED BY A COMA
			lineTokenizer.setDelimiter(",");
			lineTokenizer.setStrict(false);
			//FIELDS IN CSV FILE
			lineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob");
			
			//PASS THE DATA TO THE CUSTOMER CLASS
			BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
			fieldSetMapper.setTargetType(Customer.class);
			
			lineMapper.setLineTokenizer(lineTokenizer);
			lineMapper.setFieldSetMapper(fieldSetMapper);
			return lineMapper;
			
			
	}
	
	//------------------------------2. ItemProcessor-------------------------------------
	@Bean
	public CustomerProcessor processor() {
		return new CustomerProcessor();
	}
	
	
	//------------------------------3. ItemWritter-------------------------------------
	@Bean
	public RepositoryItemWriter<Customer> writer(){
		
		RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
		writer.setRepository(customerRepository);
		writer.setMethodName("save");
		return writer;
		
	}

	
	//------------------------------4. CREATE STEP OBJECT-------------------------------------
	@Bean
	public Step step1() {
		return new StepBuilder("csv-step",jobRepository).<Customer,Customer>chunk(10, transactionManager)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
	
	
	
	//------------------------------5. CREATE JOB OBJECT-------------------------------------
	
	@Bean
	public Job runJob() {
		return new JobBuilder("importCustomers",jobRepository)
				.start(step1())
				//.end()
				.build();
	}
	
	
	
	
	
	
	
	
	
	
	

}
