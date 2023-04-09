package fi.aalto.forum;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
class ForumApplicationTests implements ApplicationContextAware {
	private org.springframework.context.ApplicationContext applicationContext;
	@Test
	void contextLoads() {
		System.out.println(applicationContext);
	}

	@Override
	public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
