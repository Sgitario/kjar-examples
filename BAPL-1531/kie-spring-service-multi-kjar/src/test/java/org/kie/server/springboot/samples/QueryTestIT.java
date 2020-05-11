package org.kie.server.springboot.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.Task;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QueryTestIT {

    private static final String CONTAINER_ID = "kjar-without-parent-1.0-SNAPSHOT";
    private static final String PROCESS_ID = "evaluation";

    private int port = 8090;

	private String user = "john";
	private String password = "john@pwd1";

	private KieServicesClient kieServicesClient;

	private QueryTestUtil queryUtils;

	private static final int PROCESS_AMOUNT = 10;
	private static final int VARIATIONS = 2;

	@Before
	public void setup() {
		queryUtils = new QueryTestUtil();
		String serverUrl = "http://localhost:" + port + "/rest/server";
		KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(serverUrl, user, password);
		configuration.setTimeout(60000);
		configuration.setMarshallingFormat(MarshallingFormat.JSON);

		List<String> cap = new ArrayList<String>();
		cap.add("COMMONS_QUERY");
		cap.add("BPM");
		cap.add("BRM");
		cap.add("CaseMgmt");

		configuration.setCapabilities(cap);

		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(Task.class);
		configuration.addExtraClasses(classes);

		this.kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
	}

	@Test
	public void testQueryTasksByExistingTaskVariable() throws InterruptedException, IOException {

		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
            pids.addAll(queryUtils.startProcess(PROCESS_AMOUNT, CONTAINER_ID, PROCESS_ID, processClient, tmp));
		}

        int total = processClient.findProcessInstances(CONTAINER_ID, 0, 0).size();
		assertThat(total, is(VARIATIONS * PROCESS_AMOUNT));

        processClient.abortProcessInstances(CONTAINER_ID, pids);
	}
}
