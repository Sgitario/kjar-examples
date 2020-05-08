package org.kie.server.springboot.samples;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.appformer.maven.integration.MavenRepository;
import org.jbpm.services.api.DeploymentService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.services.api.KieServer;
import org.kie.server.springboot.jbpm.ContainerAliasResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.example.Attribute;
import com.example.KieQueryServicesClient;
import com.example.Task;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { KieServerApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Ignore
public class QueryTest {

	static final String ARTIFACT_ID = "Sample";
	static final String GROUP_ID = "com.myspace";
	static final String VERSION = "1.0.3-SNAPSHOT";
	private static final Logger logger = LoggerFactory.getLogger(QueryTest.class);

	@LocalServerPort
	private int port;

	private String user = "john";
	private String password = "john@pwd1";

	private String containerAlias = "search";
	private String containerId = "search";

	private String processId = "bpm-commons-test";

	private KieServicesClient kieServicesClient;

	@Autowired
	private KieServer kieServer;

	private QueryTestUtil queryUtils;

	@Autowired
	private ContainerAliasResolver aliasResolver;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private DeploymentService deploymentService;

	private static final int PROCESS_AMOUNT = 10;
	private static final int VARIATIONS = 2;

//	@BeforeClass
//	public static void generalSetup() {
//		System.setProperty(KieServerConstants.KIE_SERVER_MODE, KieServerMode.DEVELOPMENT.name());
//		KieServices ks = KieServices.Factory.get();
//		org.kie.api.builder.ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
//		File kjar = new File("../kjars/search/Sample-1.0.3-SNAPSHOT.jar");
//		File pom = new File("../kjars/search/pom.xml");
//		MavenRepository repository = getMavenRepository();
//		repository.installArtifact(releaseId, kjar, pom);
//	}

	@AfterClass
	public static void generalCleanup() {
		System.clearProperty(KieServerConstants.KIE_SERVER_MODE);
	}

	@Before
	public void setup() {
		queryUtils = new QueryTestUtil();
		ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
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
//
		KieContainerResource resource = new KieContainerResource(containerId, releaseId);
		resource.setContainerAlias(containerAlias);
		kieServicesClient.createContainer(containerId, resource);

//		KieContainer kieContainer = KieServices.Factory.get().newKieClasspathContainer();
//
//		CustomIdKModuleDeploymentUnit unit = new CustomIdKModuleDeploymentUnit(containerId, "test", "test",
//				"1.0.0-SNAPSHOT");
//		unit.setKieContainer(kieContainer);
//		logger.info("deploying {}", containerId);
//		deploymentService.deploy(unit);
	}

	@After
	public void cleanup() {
		if (kieServicesClient != null) {
			kieServicesClient.disposeContainer(containerId);
		}
	}

	@Test
	public void testQueryTasksByExistingTaskVariable() throws InterruptedException, IOException {

		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
		List<ProcessInstance> pi = queryClient.findProcessInstancesByStatus(Arrays.asList(1), 0, 0);

		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			pids.addAll(queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp));
		}

		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(VARIATIONS * PROCESS_AMOUNT));

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("t_" + QueryTestUtil.KEY_ASSIGNEE, tmp.get(QueryTestUtil.KEY_ASSIGNEE));
			List<Task> tasks = searchClient.queryTasks(criteria, false);
			assertThat(tasks.size(), is(PROCESS_AMOUNT));
			tasks.forEach(t -> {

				assertThat(t.getTaskVariables().get(QueryTestUtil.KEY_ASSIGNEE),
						is(criteria.get("t_" + QueryTestUtil.KEY_ASSIGNEE)));
			});

		}

		processClient.abortProcessInstances(containerId, pids);

	}

	@Test
	public void testQueryTasksByNonExistingTaskVariable() throws InterruptedException, IOException {

		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			pids.addAll(queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp));
		}

		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(VARIATIONS * PROCESS_AMOUNT));

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("t_" + QueryTestUtil.KEY_ASSIGNEE,
					tmp.get(QueryTestUtil.KEY_ASSIGNEE) + RandomStringUtils.randomAlphabetic(10));
			List<Task> tasks = searchClient.queryTasks(criteria, false);
			assertThat(tasks.size(), is(0));

		}

		processClient.abortProcessInstances(containerId, pids);

	}

	@Test
	public void testQueryTasksByTaskAttributes() throws IOException {
		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		Map<String, Object> tmp = queryUtils.createPayload(0);
		pids.addAll(queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp));

		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT));

		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put(Attribute.TASK_NAME.name(), tmp.get(Attribute.TASK_NAME.name()));
		List<Task> tasks = searchClient.queryTasks(criteria, false);

		tasks.forEach(t -> {
			assertThat(t.getName(), is(criteria.get(Attribute.TASK_NAME.name())));
		});

		assertThat(tasks.size(), is(PROCESS_AMOUNT));
		processClient.abortProcessInstances(containerId, pids);

	}

	@Test
	public void testQueryTasksByNonExistingAttributes() throws IOException {
		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		Map<String, Object> tmp = queryUtils.createPayload(0);
		pids.addAll(queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp));

		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT));

		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put(Attribute.TASK_NAME.name() + RandomStringUtils.randomAlphabetic(10),
				tmp.get(Attribute.TASK_NAME.name()));
		List<Task> tasks = searchClient.queryTasks(criteria, false);

		assertThat(tasks.size(), is(0));
		processClient.abortProcessInstances(containerId, pids);

	}

	@Test
	public void testQueryTasksByProcessAttributes() throws IOException {
		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		Map<String, Object> tmp = queryUtils.createPayload(0);
		pids.addAll(queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp));
		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT));

		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put(Attribute.PROCESS_ID.name(), tmp.get(Attribute.PROCESS_ID.name()));
		List<Task> tasks = searchClient.queryTasks(criteria, false);
		assertThat(tasks.size(), is(PROCESS_AMOUNT));
		tasks.forEach(t -> {

			assertThat(t.getProcessId(), is(criteria.get(Attribute.PROCESS_ID.name())));
		});
		processClient.abortProcessInstances(containerId, pids);

	}

	@Test
	public void testQueryTasksByTaskAttributesAndProcessVariables() throws IOException {
		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			pids.addAll(queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp));
		}
		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT * VARIATIONS));

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("p_" + QueryTestUtil.KEY_ASSIGNEE, tmp.get(QueryTestUtil.KEY_ASSIGNEE));
			criteria.put(Attribute.TASK_NAME.name(), tmp.get(Attribute.TASK_NAME.name()));
			List<Task> tasks = searchClient.queryTasks(criteria, true);
			assertThat(tasks.size(), is(PROCESS_AMOUNT));
			tasks.forEach(t -> {

				assertThat(t.getProcessVariables().get(QueryTestUtil.KEY_ASSIGNEE),
						is(criteria.get("p_" + QueryTestUtil.KEY_ASSIGNEE)));
				assertThat(t.getName(), is(criteria.get(Attribute.TASK_NAME.name())));
			});

		}

		processClient.abortProcessInstances(containerId, pids);
	}

	@Test
	public void testQueryTasksByTaskAttributesAndVariables() throws IOException {
		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			List<Long> tmppids = queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp);

			pids.addAll(tmppids);
		}
		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT * VARIATIONS));

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("t_" + QueryTestUtil.KEY_ASSIGNEE, tmp.get(QueryTestUtil.KEY_ASSIGNEE));
			criteria.put(Attribute.POTENTIAL_OWNER.name(), tmp.get(QueryTestUtil.KEY_ASSIGNEE));
			List<Task> tasks = searchClient.queryTasks(criteria, true);
			assertThat(tasks.size(), is(PROCESS_AMOUNT));
			tasks.forEach(t -> {

				assertThat(t.getTaskVariables().get(QueryTestUtil.KEY_ASSIGNEE),
						is(criteria.get("t_" + QueryTestUtil.KEY_ASSIGNEE)));
			});

		}

		processClient.abortProcessInstances(containerId, pids);
	}

	@Test
	public void testQueryTasksByProcessAttributesAndProcessVariables() throws IOException {

		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			List<Long> tmppids = queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp);

			pids.addAll(tmppids);
		}

		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT * VARIATIONS));

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("p_" + QueryTestUtil.KEY_REVIEWER, tmp.get(QueryTestUtil.KEY_REVIEWER));
			criteria.put(Attribute.PROCESS_ID.name(), tmp.get(Attribute.PROCESS_ID.name()));
			List<Task> tasks = searchClient.queryTasks(criteria, true);
			assertThat(tasks.size(), is(PROCESS_AMOUNT));
			tasks.forEach(t -> {

				assertThat(t.getProcessVariables().get(QueryTestUtil.KEY_REVIEWER),
						is(criteria.get("p_" + QueryTestUtil.KEY_REVIEWER)));
				assertThat(t.getProcessId(), is(criteria.get(Attribute.PROCESS_ID.name())));
			});

		}

	}

	@Test
	public void testQueryTasksByGroups() throws IOException, InterruptedException {
		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		Map<String, Object> tmp = queryUtils.createPayload(0);
		List<Long> tmppids = queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp);

		pids.addAll(tmppids);

		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put(Attribute.POTENTIAL_OWNER.name(), tmp.get(QueryTestUtil.KEY_GROUP));
		List<Task> tasks = searchClient.queryTasks(criteria, false);
		assertThat(tasks.size(), is(PROCESS_AMOUNT));
		List<String> searchGroups = Arrays.asList(criteria.get(Attribute.POTENTIAL_OWNER.name()).toString().split(","));
		tasks.forEach(t -> {

			t.getGroups().forEach(g -> {

				assertTrue(searchGroups.contains(g));
			});

		});
		processClient.abortProcessInstances(containerId, pids);

	}

	@Test
	public void testQueryTasksByProcessAttributesAndTaskVariables() throws IOException {

		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			List<Long> tmppids = queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp);

			pids.addAll(tmppids);
		}

		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT * VARIATIONS));

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put("t_" + QueryTestUtil.KEY_ASSIGNEE, tmp.get(QueryTestUtil.KEY_ASSIGNEE));
			criteria.put(Attribute.PROCESS_ID.name(), tmp.get(Attribute.PROCESS_ID.name()));
			List<Task> tasks = searchClient.queryTasks(criteria, true);
			assertThat(tasks.size(), is(PROCESS_AMOUNT));
			tasks.forEach(t -> {

				assertThat(t.getTaskVariables().get(QueryTestUtil.KEY_REVIEWER),
						is(criteria.get("t_" + QueryTestUtil.KEY_REVIEWER)));
				assertThat(t.getProcessId(), is(criteria.get(Attribute.PROCESS_ID.name())));
			});

		}
	}

	@Test
	@Ignore
	public void testQueryTasksByTaskOwner() throws IOException {

		ProcessServicesClient processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		KieQueryServicesClient searchClient = kieServicesClient.getServicesClient(KieQueryServicesClient.class);
		UserTaskServicesClient userClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);
		List<Long> pids = new ArrayList<Long>();

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			List<Long> tmppids = queryUtils.startProcess(PROCESS_AMOUNT, containerId, processId, processClient, tmp);
			String user = tmp.get(QueryTestUtil.KEY_ASSIGNEE).toString();

			tmppids.forEach(tmppid -> {
				List<TaskSummary> ts = userClient.findTasksByStatusByProcessInstanceId(tmppid, new ArrayList<String>(),
						0, 0);
				logger.info("{} task(s) found for pid {}", ts.size(), tmppid);
				ts.forEach(tstmp -> {
					userClient.claimTask(containerId, tstmp.getId(), user);

				});

			});

			pids.addAll(tmppids);
		}

		int total = processClient.findProcessInstances(containerId, 0, 0).size();
		assertThat(total, is(PROCESS_AMOUNT * VARIATIONS));

		for (int i = 0; i < VARIATIONS; i++) {
			Map<String, Object> tmp = queryUtils.createPayload(i);
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put(Attribute.ACTUAL_OWNER.name(), tmp.get(QueryTestUtil.KEY_ASSIGNEE));
			List<Task> tasks = searchClient.queryTasks(criteria, true);
			assertThat(tasks.size(), is(PROCESS_AMOUNT));
			tasks.forEach(t -> {

				assertThat(t.getActualOwner(), is(criteria.get(Attribute.ACTUAL_OWNER.name())));
			});

		}

	}

}
