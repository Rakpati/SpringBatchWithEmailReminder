package com.batchRun.util;

import java.io.IOException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.sql.DataSource;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class MyTasklet implements Tasklet{

	private static DataSource dataSource;
	private static final String countQuery="select count(*) from Hdbstatus";
	private static final String selectQuery="select * from Hdbstatus order by id LIMIT ?, ?";
	private static final String updateQuery="update Hdbstatus set status = 'lapse' where id = ?";
	String str = "classpath:META-INF/spring/Spring-mail.xml";

	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		int recordCount = 0, lowerLimit = 0, upperLimit = 50;
		List<HdbStatus> hdbStatus = new ArrayList<HdbStatus>();
		JdbcTemplate myTemplate = new JdbcTemplate(getDataSource());
		recordCount = myTemplate.queryForInt(countQuery);

		System.out.println(recordCount);
		while(lowerLimit <= recordCount) {
			Object[] params = { lowerLimit, upperLimit};
			int[] types = {Types.INTEGER,Types.INTEGER};
			
			hdbStatus = myTemplate.query(selectQuery, params, types, new HdbStatusMapper());
			for(HdbStatus p: hdbStatus){
				checkDate(p);
				System.out.println(p.toString());
			}
			lowerLimit = lowerLimit + upperLimit;
		}
		return RepeatStatus.FINISHED;
	}

	private void checkDate(HdbStatus hdbStatus) throws Exception {
		LocalDate localDate = LocalDate.now();
		final TreeSet<String> name = new TreeSet<String>();
		final TreeSet<String> email = new TreeSet<String>();
		final TreeSet<Integer> days = new TreeSet<Integer>();

		Date date1 = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		System.out.println("Date      = " + date1);
		System.out.println(date1.compareTo(hdbStatus.getStatusDate()));
		
		long difference = date1.getTime() - hdbStatus.getStatusDate().getTime();
		int daysBetween = (int) (difference / (1000*60*60*24));
		System.out.println(daysBetween);
		
		if(hdbStatus.getStatus().equalsIgnoreCase("due") && daysBetween > 2) {
			ApplicationContext ctx = new ClassPathXmlApplicationContext(str);
			MailMail mm = (MailMail) ctx.getBean("mailMail");
			name.add(hdbStatus.getName());
			email.add(hdbStatus.getEmailId());
			days.add(daysBetween);

			if(daysBetween == 3 || daysBetween == 7){
				mm.sendMail(name, email, days);
			}else if(daysBetween == 21) {
				mm.sendMail(name, email, days);
				updateRecord(hdbStatus.getId());
			}
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public static void updateRecord(int id) {

		JdbcTemplate template = new JdbcTemplate(dataSource);
		Object[] params = { id};

		int[] types = {Types.INTEGER};
		int rows = template.update(updateQuery, params, types);

		System.out.println(rows + " row(s) updated.");
	}
}
