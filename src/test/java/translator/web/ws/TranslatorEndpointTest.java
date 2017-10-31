package translator.web.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

import translator.Application;
import translator.web.ws.schema.GetTranslationRequest;
import translator.web.ws.schema.GetTranslationResponse;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= WebEnvironment.RANDOM_PORT, classes = Application.class)
public class TranslatorEndpointTest {

	private Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

	@LocalServerPort
	private int port;

	@Before
	public void init() throws Exception {
		marshaller.setPackagesToScan(ClassUtils.getPackageName(GetTranslationRequest.class));
		marshaller.afterPropertiesSet();
	}
	
	public Wss4jSecurityInterceptor securityInterceptor1(){
        Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
        wss4jSecurityInterceptor.setSecurementActions("Timestamp UsernameToken");
        wss4jSecurityInterceptor.setSecurementUsername("admin");
        wss4jSecurityInterceptor.setSecurementPassword("secret");
        return wss4jSecurityInterceptor;
    }
	
	public Wss4jSecurityInterceptor securityInterceptor2(){
        Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
        wss4jSecurityInterceptor.setSecurementActions("Timestamp UsernameToken");
        wss4jSecurityInterceptor.setSecurementUsername("admin");
        wss4jSecurityInterceptor.setSecurementPassword("secrete");
        return wss4jSecurityInterceptor;
    }
	
	@Test
	public void testSendAndReceiveOK() {
		GetTranslationRequest request = new GetTranslationRequest();
		request.setLangFrom("zh");
		request.setLangTo("en");
		request.setText("这是一个试验的翻译服务");
		ClientInterceptor[] interceptors = new ClientInterceptor[] {securityInterceptor1()};
		
		WebServiceTemplate webService = new WebServiceTemplate(marshaller);
		webService.setInterceptors(interceptors);
		Object response = webService. marshalSendAndReceive("http://localhost:"
				+ port + "/ws", request);
		assertNotNull(response);
		assertThat(response, instanceOf(GetTranslationResponse.class));
		GetTranslationResponse translation = (GetTranslationResponse) response;
		assertThat(translation.getTranslation(), is("This is a test of the translation service"));
	}	
	
	
	@Test(expected=SoapFaultClientException.class)
	public void testSendAndReceiveBAD() {
		GetTranslationRequest request = new GetTranslationRequest();
		request.setLangFrom("zh");
		request.setLangTo("en");
		request.setText("这是一个试验的翻译服务");
		ClientInterceptor[] interceptors = new ClientInterceptor[] {securityInterceptor2()};
		
		WebServiceTemplate webService = new WebServiceTemplate(marshaller);
		webService.setInterceptors(interceptors);
		webService. marshalSendAndReceive("http://localhost:"
				+ port + "/ws", request);
	}	
}
