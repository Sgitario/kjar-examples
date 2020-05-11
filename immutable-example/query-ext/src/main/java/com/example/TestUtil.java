package com.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TestUtil {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		
		 boolean result = EnumUtils.isValidEnum(Attribute.class, "TASK_NAME");
		 System.out.println(result);
		 boolean result2 = EnumUtils.isValidEnum(Attribute.class, "TASK_NAME_SHIT");
		 System.out.println(result2);
		 
		 System.exit(-1);

		
		String group = "group1,group2";
		
		System.out.println(Arrays.toString(group.split(",")));
		System.exit(-1);

		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(Task.class);
		classes.add(List.class);


		Marshaller m = MarshallerFactory.getMarshaller(classes, MarshallingFormat.JSON,
				TestUtil.class.getClassLoader());
		
		String source = "[{\"com.example.BPMTask\" : {\"taskId\":66,\"processInstanceId\":66,\"owner\":\"anton\",\"name\":\"SampleTask3-new\",\"taskVariables\":{\"AnotherTaskVariable\":\"value3\",\"SampleTaskVariable\":\"value4\"},\"processVariables\":{\"AnotherVariable\":\"value3\",\"initiator\":\"anton\",\"SampleProcessVariable\":\"value4\"}}},{\"com.example.BPMTask\" : {\"taskId\":65,\"processInstanceId\":65,\"owner\":\"anton\",\"name\":\"SampleTask2-new\",\"taskVariables\":{\"AnotherTaskVariable\":\"value3\",\"SampleTaskVariable\":\"value4\"},\"processVariables\":{\"AnotherVariable\":\"value3\",\"initiator\":\"anton\",\"SampleProcessVariable\":\"value4\"}}}]";
		
		System.out.println(source);
		List<Task> tasks = m.unmarshall(source, List.class);
		
		tasks.forEach(t -> {
			
			System.out.println(tasks);
		});

	}

}
