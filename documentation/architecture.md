# Architecture

## Table of Contents
- [Architecture](#architecture)
  - [Table of Contents](#table-of-contents)
  - [Package Structure](#package-structure)
  - [Use Cases](#use-cases)
    - [Use Case 1: Start FA<sup>3</sup>ST Service and Retrieve Value](#use-case-1-start-fasup3supst-service-and-retrieve-value)
    - [Use Case 2: Set Value with FA<sup>3</sup>ST to an Assetconnection](#use-case-2-set-value-with-fasup3supst-to-an-assetconnection)
    - [Use Case 3: Set a property value via HTTP endpoint and with an running internal OPC UA model with FA<sup>3</sup>ST](#use-case-3-set-a-property-value-via-http-endpoint-and-with-an-running-internal-opc-ua-model-with-fasup3supst)
    - [Use Case 4: Add a new SubmodelElement via HTTP endpoint and with an running internal OPC UA model with FA<sup>3</sup>ST](#use-case-4-add-a-new-submodelelement-via-http-endpoint-and-with-an-running-internal-opc-ua-model-with-fasup3supst)

## Package Structure

The package structure of the FA³ST Service consists of 10 packages. Each package has a subpackage "Interfaces" which contains the general interfaces for the package to be used in the implementation classes. In the subpackage "Model" should contain model classes that are used only within the package. A list of all packages:
- Endpoints: 
- Serializers
- Configuration
- MessageBus
- Core
- AssetConnections
- Persistence
- Model
- Util
- Configuration

![Package Structure](PackageStructure.png)

## Use Cases

### Use Case 1: Start FA<sup>3</sup>ST Service and Retrieve Value

**Actions**:
1. Create FA³ST service with config file
2. Start FA³ST service
3. Get value over HTTP of property “Property1”

**Structural Assumptions**
```bash
_AAS("AAS1")
├── _Submodel("Submodel1")
│   ├── _Property("Property1")
│   │   ├── value("test")
```

**Technical Assumptions**

| HTTP_Endpoint | OPCUA_Endpoint | Persistence | Asset Connection | Message Bus |
|---------------|----------------|-------------|------------------|-------------|
| Yes           | No             | In Memory   | No               | Internal    |


**Sequence Diagram**   
Create Service
![Use Case 1](SequenceDiagrams/img/UseCase1_Create_Service.svg)   

Get Value via HTTP
![Use Case 1](SequenceDiagrams/img/UseCase1_Get_Value_HTTP.svg)

<br>

---

<br>

### Use Case 2: Set Value with FA<sup>3</sup>ST to an Assetconnection

**Actions**:
1. Set Value "NewValue" via HTTP of property "Property1"
2. FA³ST sets value of property in internal memory
2. FA³ST transfers value via the assetconnection to the asset

**Structural Assumptions**
```bash
_AAS("AAS1")
├── _Submodel("Submodel1")
│   ├── _Property("Property1")
│   │   ├── value("test")
```

**Technical Assumptions**

| HTTP_Endpoint | OPCUA_Endpoint | Persistence | Asset Connection | Message Bus |
|---------------|----------------|-------------|------------------|-------------|
| Yes           | No             | In Memory   | Yes               | Internal    |


**Sequence Diagram**

Create Service
![Use Case 2](SequenceDiagrams/img/UseCase2_Create_Service.svg)

Set Value via HTTP
![Use Case 2](SequenceDiagrams/img/UseCase2_Set_Value_Via_HTTP.svg)

<br>

---

<br>

### Use Case 3: Set a property value via HTTP endpoint and with an running internal OPC UA model with FA<sup>3</sup>ST

**Actions**:
1. OPC UA Endpoint subscribes to all write events in message bus
2. User sets Value "NewValue" via HTTP endpoint of property "Property1"
3. FA³ST sets value of property in internal memory
4. FA³ST publish a write event via the internal message bus
5. OPC UA Endpoint capture writing element and update internal OPC UA model
6. User reads current value via OPC UA endpoint

**Structural Assumptions**
```bash
_AAS("AAS1")
├── _Submodel("Submodel1")
│   ├── _Property("Property1")
│   │   ├── value("test")
```

**Technical Assumptions**

| HTTP_Endpoint | OPCUA_Endpoint | Persistence | Asset Connection | Message Bus |
|---------------|----------------|-------------|------------------|-------------|
| Yes           | Yes             | In Memory   | No               | Internal    |

**Comments**
- The sequence diagram "Create Service" is not shown. It is almost the same as the previous service creations but with additional creation of an OPCUA endpoint
- The used implementation of the OPC UA endpoint holds an internal model 

**Sequence Diagram**

Create Service
![Use Case 3](SequenceDiagrams/img/UseCase3_Create_Service.svg)

Set Value and retrieve the same Value via OPC
![Use Case 3](SequenceDiagrams/img/UseCase3_Set_Value_and_retrieve_via_OPC.svg)

<br>

---

<br>

### Use Case 4: Add a new SubmodelElement via HTTP endpoint and with an running internal OPC UA model with FA<sup>3</sup>ST

**Actions**:
1. OPC UA Endpoint subscribes to all write events in message bus
2. User add new SubmodelElement "Property2" via HTTP endpoint
3. FA³ST add new SubmodelElement in internal memory
4. FA³ST publish a write event via the internal message bus
5. OPC UA Endpoint capture writing element and update internal OPC UA model
6. User reads new SubmodelElement via OPC UA endpoint

**Structural Assumptions**
```bash
_AAS("AAS1")
├── _Submodel("Submodel1")
│   ├── _Property("Property1")
│   │   ├── value("test")
```

**Technical Assumptions**

| HTTP_Endpoint | OPCUA_Endpoint | Persistence | Asset Connection | Message Bus |
|---------------|----------------|-------------|------------------|-------------|
| Yes           | Yes             | In Memory   | No               | Internal    |

**Comments**
- The sequence diagram "Create Service" is not shown. It is almost the same as the previous service creations but with additional creation of an OPCUA endpoint
- The used implementation of the OPC UA endpoint holds an internal model 

**Sequence Diagram**

Create Service
![Use Case 4](SequenceDiagrams/img/UseCase4_Create_Service.svg)

Add new SobmodelElement via HTTP and retrieve the new SubmodelElement via OPC
![Use Case 4](SequenceDiagrams/img/UseCase4_Add_new_SubmodelElement_via_HTTP_and_retrieve_via_OPC.svg)