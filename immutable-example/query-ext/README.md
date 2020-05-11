KIE Server Search Extension
============================
The purpose of this extension is to provide advanced search capabilities to kie-server until they are officially part of the product as out-of-the-box feature.



Installation
------------------------------

The project needs to be built via `mvn clean install` command and then added on the kie-server classpath. Either by installing the `*.jar` file in WEB-INF/lib folder or in case of embedding kie-server via spring boot, simply add it as a maven dependency.


Configuration
------------------------------
The only required configuration is to make sure that the extension project bundles file named `org.kie.server.services.api.KieServerApplicationComponentsService` in `src/main/resources/META-INF/services` with content:

```
fully qualified class name of class implementing KieServerApplicationComponentsService, i.e.:
com.example.QueryExtensionComponentsService
```

Usage:
------------------------------
The extension will support three separate endpoints which exposes different flavours of search functionality:
```
 - server/queries/search/tasks
 - server/queries/search/cases (not implemented yet)
 - server/queries/search/processes (not implemented yet)
 ```

 All endpoints are accessible via POST request and they consume and produces JSON.


####  server/queries/search/tasks

**Example REST Request:**

```
curl --location --request POST 'http://localhost:8080/kie-server/services/rest/server/queries/search/tasks' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YW50b246cGFzc3dvcmQxIQ==' \
--data-raw '{
    "searchCriteria": {
              "t_SampleTaskVariable" : "value4",
              "POTENTIAL_OWNER": "randomGroup"
              
         },
         "appendProcessVars": true
        
}'

```

The request object contains a child object with name `searchCriteria` which contains the actual search criterias in a key-value format.

 - If the key starts with `t_` the extension will consider this as a Task variable
 - If the key starts with `p_` the extension will consider this as a Process variable
 - If the key does not start with `t_` or `p_` then it can also accepts the following keywords (which are tasks or process attributes):
   - PROCESS_INSTANCE_ID
   - ACTUAL_OWNER
   - POTENTIAL_OWNER (at the moment only groups are supported)
     - This parameter can accept multiple values in form of a comma separate list, i.e.: group1, group2, group3 - there is implicit OR between the respective groups
   - BUSINESS_KEY (maps to Correlation Key in the RHPAM)
   - PROCESS_ID
   - TASK_NAME

There is implicit `AND` between all the criterias. Only one occurence of each search criteria is supported at the moment.
Apart from searchCriteria object specified in the request payload, there is one more object supported, and that is `appendProcessVars`. It can be `true` or `false` and depending on the value the result will include or not include the process variables associated with the given task. All the values should be provided as `string`.

**Example REST Response:**

The response is array of tasks with all the task variables and optionally all the process variables associated with it:
```
[
  {
    "com.example.Task": {
      "taskId": 2,
      "processInstanceId": 2,
      "actualOwner": "anton",
      "name": "SampleTask3-new",
      "processId": "Sample.SampleProcess",
      "correlationKeyName": null,
      "taskVariables": {
        "AnotherTaskVariable": "value3",
        "SampleTaskVariable": "value4"
      },
      "processVariables": {
        "AnotherVariable": "value3",
        "initiator": "anton",
        "SampleProcessVariable": "value4"
      },
      "groups": [
        "test",
        "randomGroup",
        "kie-server"
      ]
    }
  },
  {
    "com.example.Task": {
      "taskId": 1,
      "processInstanceId": 1,
      "actualOwner": null,
      "name": "SampleTask3-new",
      "processId": "Sample.SampleProcess",
      "correlationKeyName": "MyKey1",
      "taskVariables": {
        "AnotherTaskVariable": "value3",
        "SampleTaskVariable": "value4"
      },
      "processVariables": {
        "AnotherVariable": "value3",
        "initiator": "anton",
        "SampleProcessVariable": "value4"
      },
      "groups": [
        "test",
        "randomGroup",
        "kie-server"
      ]
    }
  }
]
```

`actualOwner` can be null if the task is not owned by anybody (i.e. it is in Ready state).
`correlationKeyName` can be also null if the process was started without one.

####  server/queries/search/processes

**Example REST Request:**

```
curl --location --request POST 'http://localhost:8080/kie-server/services/rest/server/queries/search/processes' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YW50b246cGFzc3dvcmQxIQ==' \
--data-raw '{
    "searchCriteria": {
              "p_sampleProcessVar" : "SampleValue"
         },
         "appendProcessVars": true
        
}'
```

**Example REST Response:**
```
[
  {
    "com.example.Process": {
      "instanceId": 126,
      "id": "Sample.SameTaskInputOutputVariable",
      "correlationKeyName": null,
      "processVariables": {
        "sampleProcessVar": "SampleValue",
        "initiator": "anton"
      }
    }
  }
]
```

The input search criteria as well as implementaion details are identical to task search operation.






