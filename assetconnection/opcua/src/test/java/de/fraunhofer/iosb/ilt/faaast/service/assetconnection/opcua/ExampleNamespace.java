/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ulong;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.ValueRank;
import org.eclipse.milo.opcua.sdk.server.Lifecycle;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.BaseEventTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.AnalogItemTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.factories.NodeFactory;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExampleNamespace extends ManagedNamespaceWithLifecycle {

    public static final String NAMESPACE_URI = "urn:eclipse:milo:hello-world";

    private static final Object[][] STATIC_SCALAR_NODES = new Object[][] {
            {
                    "Boolean",
                    Identifiers.Boolean,
                    new Variant(false)
            },
            {
                    "Byte",
                    Identifiers.Byte,
                    new Variant(ubyte(0x00))
            },
            {
                    "SByte",
                    Identifiers.SByte,
                    new Variant((byte) 0x00)
            },
            {
                    "Integer",
                    Identifiers.Integer,
                    new Variant(32)
            },
            {
                    "Int16",
                    Identifiers.Int16,
                    new Variant((short) 16)
            },
            {
                    "Int32",
                    Identifiers.Int32,
                    new Variant(32)
            },
            {
                    "Int64",
                    Identifiers.Int64,
                    new Variant(64L)
            },
            {
                    "UInteger",
                    Identifiers.UInteger,
                    new Variant(uint(32))
            },
            {
                    "UInt16",
                    Identifiers.UInt16,
                    new Variant(ushort(16))
            },
            {
                    "UInt32",
                    Identifiers.UInt32,
                    new Variant(uint(32))
            },
            {
                    "UInt64",
                    Identifiers.UInt64,
                    new Variant(ulong(64L))
            },
            {
                    "Float",
                    Identifiers.Float,
                    new Variant(3.14f)
            },
            {
                    "Double",
                    Identifiers.Double,
                    new Variant(3.14d)
            },
            {
                    "String",
                    Identifiers.String,
                    new Variant("string value")
            },
            {
                    "DateTime",
                    Identifiers.DateTime,
                    new Variant(DateTime.now())
            },
            {
                    "Guid",
                    Identifiers.Guid,
                    new Variant(UUID.randomUUID())
            },
            {
                    "ByteString",
                    Identifiers.ByteString,
                    new Variant(new ByteString(new byte[] {
                            0x01,
                            0x02,
                            0x03,
                            0x04
                    }))
            },
            {
                    "XmlElement",
                    Identifiers.XmlElement,
                    new Variant(new XmlElement("<a>hello</a>"))
            },
            {
                    "LocalizedText",
                    Identifiers.LocalizedText,
                    new Variant(LocalizedText.english("localized text"))
            },
            {
                    "QualifiedName",
                    Identifiers.QualifiedName,
                    new Variant(new QualifiedName(1234, "defg"))
            },
            {
                    "NodeId",
                    Identifiers.NodeId,
                    new Variant(new NodeId(1234, "abcd"))
            },
            {
                    "Variant",
                    Identifiers.BaseDataType,
                    new Variant(32)
            },
            {
                    "Duration",
                    Identifiers.Duration,
                    new Variant(1.0)
            },
            {
                    "UtcTime",
                    Identifiers.UtcTime,
                    new Variant(DateTime.now())
            },
    };

    private static final Object[][] STATIC_ARRAY_NODES = new Object[][] {
            {
                    "BooleanArray",
                    Identifiers.Boolean,
                    false
            },
            {
                    "ByteArray",
                    Identifiers.Byte,
                    ubyte(0)
            },
            {
                    "SByteArray",
                    Identifiers.SByte,
                    (byte) 0x00
            },
            {
                    "Int16Array",
                    Identifiers.Int16,
                    (short) 16
            },
            {
                    "Int32Array",
                    Identifiers.Int32,
                    32
            },
            {
                    "Int64Array",
                    Identifiers.Int64,
                    64L
            },
            {
                    "UInt16Array",
                    Identifiers.UInt16,
                    ushort(16)
            },
            {
                    "UInt32Array",
                    Identifiers.UInt32,
                    uint(32)
            },
            {
                    "UInt64Array",
                    Identifiers.UInt64,
                    ulong(64L)
            },
            {
                    "FloatArray",
                    Identifiers.Float,
                    3.14f
            },
            {
                    "DoubleArray",
                    Identifiers.Double,
                    3.14d
            },
            {
                    "StringArray",
                    Identifiers.String,
                    "string value"
            },
            {
                    "DateTimeArray",
                    Identifiers.DateTime,
                    DateTime.now()
            },
            {
                    "GuidArray",
                    Identifiers.Guid,
                    UUID.randomUUID()
            },
            {
                    "ByteStringArray",
                    Identifiers.ByteString,
                    new ByteString(new byte[] {
                            0x01,
                            0x02,
                            0x03,
                            0x04
                    })
            },
            {
                    "XmlElementArray",
                    Identifiers.XmlElement,
                    new XmlElement("<a>hello</a>")
            },
            {
                    "LocalizedTextArray",
                    Identifiers.LocalizedText,
                    LocalizedText.english("localized text")
            },
            {
                    "QualifiedNameArray",
                    Identifiers.QualifiedName,
                    new QualifiedName(1234, "defg")
            },
            {
                    "NodeIdArray",
                    Identifiers.NodeId,
                    new NodeId(1234, "abcd")
            }
    };

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private volatile Thread eventThread;
    private volatile boolean keepPostingEvents = true;

    private final SubscriptionModel subscriptionModel;

    ExampleNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);

        subscriptionModel = new SubscriptionModel(server, this);

        getLifecycleManager().addLifecycle(subscriptionModel);

        getLifecycleManager().addStartupTask(this::createAndAddNodes);

        getLifecycleManager().addLifecycle(new Lifecycle() {
            @Override
            public void startup() {
                startBogusEventNotifier();
            }


            @Override
            public void shutdown() {
                try {
                    keepPostingEvents = false;
                    eventThread.interrupt();
                    eventThread.join();
                }
                catch (InterruptedException ignored) {
                    // ignored
                }
            }
        });
    }


    private void createAndAddNodes() {
        // Create a "HelloWorld" folder and add it to the node manager
        NodeId folderNodeId = newNodeId("HelloWorld");

        UaFolderNode folderNode = new UaFolderNode(
                getNodeContext(),
                folderNodeId,
                newQualifiedName("HelloWorld"),
                LocalizedText.english("HelloWorld"));

        getNodeManager().addNode(folderNode);

        // Make sure our new folder shows up under the server's Objects folder.
        folderNode.addReference(new Reference(
                folderNode.getNodeId(),
                Identifiers.Organizes,
                Identifiers.ObjectsFolder.expanded(),
                false));

        // Add the rest of the nodes
        addVariableNodes(folderNode);

        addSqrtMethod(folderNode);

        addGenerateEventMethod(folderNode);

        addCustomObjectTypeAndInstance(folderNode);
    }


    private void startBogusEventNotifier() {
        // Set the EventNotifier bit on Server Node for Events.
        UaNode serverNode = getServer()
                .getAddressSpaceManager()
                .getManagedNode(Identifiers.Server)
                .orElse(null);

        if (serverNode instanceof ServerTypeNode) {
            ((ServerTypeNode) serverNode).setEventNotifier(ubyte(1));

            // Post a bogus Event every couple seconds
            eventThread = new Thread(() -> {
                while (keepPostingEvents) {
                    try {
                        BaseEventTypeNode eventNode = getServer().getEventFactory().createEvent(
                                newNodeId(UUID.randomUUID()),
                                Identifiers.BaseEventType);

                        eventNode.setBrowseName(new QualifiedName(1, "foo"));
                        eventNode.setDisplayName(LocalizedText.english("foo"));
                        eventNode.setEventId(ByteString.of(new byte[] {
                                0,
                                1,
                                2,
                                3
                        }));
                        eventNode.setEventType(Identifiers.BaseEventType);
                        eventNode.setSourceNode(serverNode.getNodeId());
                        eventNode.setSourceName(serverNode.getDisplayName().getText());
                        eventNode.setTime(DateTime.now());
                        eventNode.setReceiveTime(DateTime.NULL_VALUE);
                        eventNode.setMessage(LocalizedText.english("event message!"));
                        eventNode.setSeverity(ushort(2));

                        //noinspection UnstableApiUsage
                        getServer().getEventBus().post(eventNode);

                        eventNode.delete();
                    }
                    catch (Throwable e) {
                        LOGGER.error("Error creating EventNode: {}", e.getMessage(), e);
                    }

                    try {
                        //noinspection BusyWait
                        Thread.sleep(2_000);
                    }
                    catch (InterruptedException ignored) {
                        // ignored
                    }
                }
            }, "bogus-event-poster");

            eventThread.start();
        }
    }


    private void addVariableNodes(UaFolderNode rootNode) {
        addArrayNodes(rootNode);
        addScalarNodes(rootNode);
        addAdminReadableNodes(rootNode);
        addAdminWritableNodes(rootNode);
        addDynamicNodes(rootNode);
        addDataAccessNodes(rootNode);
        addWriteOnlyNodes(rootNode);
    }


    private void addArrayNodes(UaFolderNode rootNode) {
        UaFolderNode arrayTypesFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/ArrayTypes"),
                newQualifiedName("ArrayTypes"),
                LocalizedText.english("ArrayTypes"));

        getNodeManager().addNode(arrayTypesFolder);
        rootNode.addOrganizes(arrayTypesFolder);

        for (Object[] os: STATIC_ARRAY_NODES) {
            String name = (String) os[0];
            NodeId typeId = (NodeId) os[1];
            Object value = os[2];
            Object array = Array.newInstance(value.getClass(), 5);
            for (int i = 0; i < 5; i++) {
                Array.set(array, i, value);
            }
            Variant variant = new Variant(array);

            UaVariableNode.build(getNodeContext(), builder -> {
                builder.setNodeId(newNodeId("HelloWorld/ArrayTypes/" + name));
                builder.setAccessLevel(AccessLevel.READ_WRITE);
                builder.setUserAccessLevel(AccessLevel.READ_WRITE);
                builder.setBrowseName(newQualifiedName(name));
                builder.setDisplayName(LocalizedText.english(name));
                builder.setDataType(typeId);
                builder.setTypeDefinition(Identifiers.BaseDataVariableType);
                builder.setValueRank(ValueRank.OneDimension.getValue());
                builder.setArrayDimensions(new UInteger[] {
                        uint(0)
                });
                builder.setValue(new DataValue(variant));

                builder.addReference(new Reference(
                        builder.getNodeId(),
                        Identifiers.Organizes,
                        arrayTypesFolder.getNodeId().expanded(),
                        Reference.Direction.INVERSE));

                return builder.buildAndAdd();
            });
        }
    }


    private void addScalarNodes(UaFolderNode rootNode) {
        UaFolderNode scalarTypesFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/ScalarTypes"),
                newQualifiedName("ScalarTypes"),
                LocalizedText.english("ScalarTypes"));

        getNodeManager().addNode(scalarTypesFolder);
        rootNode.addOrganizes(scalarTypesFolder);

        for (Object[] os: STATIC_SCALAR_NODES) {
            String name = (String) os[0];
            NodeId typeId = (NodeId) os[1];
            Variant variant = (Variant) os[2];

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/ScalarTypes/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setUserAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();

            node.setValue(new DataValue(variant));

            getNodeManager().addNode(node);
            scalarTypesFolder.addOrganizes(node);
        }
    }


    private void addWriteOnlyNodes(UaFolderNode rootNode) {
        UaFolderNode writeOnlyFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/WriteOnly"),
                newQualifiedName("WriteOnly"),
                LocalizedText.english("WriteOnly"));

        getNodeManager().addNode(writeOnlyFolder);
        rootNode.addOrganizes(writeOnlyFolder);

        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/WriteOnly/" + name))
                .setAccessLevel(AccessLevel.WRITE_ONLY)
                .setUserAccessLevel(AccessLevel.WRITE_ONLY)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        node.setValue(new DataValue(new Variant("can't read this")));

        getNodeManager().addNode(node);
        writeOnlyFolder.addOrganizes(node);
    }


    private void addAdminReadableNodes(UaFolderNode rootNode) {
        UaFolderNode adminFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/OnlyAdminCanRead"),
                newQualifiedName("OnlyAdminCanRead"),
                LocalizedText.english("OnlyAdminCanRead"));

        getNodeManager().addNode(adminFolder);
        rootNode.addOrganizes(adminFolder);

        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/OnlyAdminCanRead/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        node.setValue(new DataValue(new Variant("shh... don't tell the lusers")));

        getNodeManager().addNode(node);
        adminFolder.addOrganizes(node);
    }


    private void addAdminWritableNodes(UaFolderNode rootNode) {
        UaFolderNode adminFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/OnlyAdminCanWrite"),
                newQualifiedName("OnlyAdminCanWrite"),
                LocalizedText.english("OnlyAdminCanWrite"));

        getNodeManager().addNode(adminFolder);
        rootNode.addOrganizes(adminFolder);

        String name = "String";
        UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/OnlyAdminCanWrite/" + name))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        node.setValue(new DataValue(new Variant("admin was here")));

        getNodeManager().addNode(node);
        adminFolder.addOrganizes(node);
    }


    private void addDynamicNodes(UaFolderNode rootNode) {
        UaFolderNode dynamicFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/Dynamic"),
                newQualifiedName("Dynamic"),
                LocalizedText.english("Dynamic"));

        getNodeManager().addNode(dynamicFolder);
        rootNode.addOrganizes(dynamicFolder);

        // Dynamic Boolean
        {
            String name = "Boolean";
            NodeId typeId = Identifiers.Boolean;
            Variant variant = new Variant(false);

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/Dynamic/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();

            node.setValue(new DataValue(variant));

            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }

        // Dynamic Int32
        {
            String name = "Int32";
            NodeId typeId = Identifiers.Int32;
            Variant variant = new Variant(0);

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/Dynamic/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();

            node.setValue(new DataValue(variant));

            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }

        // Dynamic Double
        {
            String name = "Double";
            NodeId typeId = Identifiers.Double;
            Variant variant = new Variant(0.0);

            UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("HelloWorld/Dynamic/" + name))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName(name))
                    .setDisplayName(LocalizedText.english(name))
                    .setDataType(typeId)
                    .setTypeDefinition(Identifiers.BaseDataVariableType)
                    .build();

            node.setValue(new DataValue(variant));

            getNodeManager().addNode(node);
            dynamicFolder.addOrganizes(node);
        }
    }


    private void addDataAccessNodes(UaFolderNode rootNode) {
        // DataAccess folder
        UaFolderNode dataAccessFolder = new UaFolderNode(
                getNodeContext(),
                newNodeId("HelloWorld/DataAccess"),
                newQualifiedName("DataAccess"),
                LocalizedText.english("DataAccess"));

        getNodeManager().addNode(dataAccessFolder);
        rootNode.addOrganizes(dataAccessFolder);

        try {
            AnalogItemTypeNode node = (AnalogItemTypeNode) getNodeFactory().createNode(
                    newNodeId("HelloWorld/DataAccess/AnalogValue"),
                    Identifiers.AnalogItemType,
                    new NodeFactory.InstantiationCallback() {
                        @Override
                        public boolean includeOptionalNode(NodeId typeDefinitionId, QualifiedName browseName) {
                            return true;
                        }
                    });

            node.setBrowseName(newQualifiedName("AnalogValue"));
            node.setDisplayName(LocalizedText.english("AnalogValue"));
            node.setDataType(Identifiers.Double);
            node.setValue(new DataValue(new Variant(3.14d)));

            node.setEURange(new Range(0.0, 100.0));

            getNodeManager().addNode(node);
            dataAccessFolder.addOrganizes(node);
        }
        catch (UaException e) {
            LOGGER.error("Error creating AnalogItemType instance: {}", e.getMessage(), e);
        }
    }


    private void addSqrtMethod(UaFolderNode folderNode) {
        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/sqrt(x)"))
                .setBrowseName(newQualifiedName("sqrt(x)"))
                .setDisplayName(new LocalizedText(null, "sqrt(x)"))
                .setDescription(
                        LocalizedText.english("Returns the correctly rounded positive square root of a double value."))
                .build();

        SqrtMethod sqrtMethod = new SqrtMethod(methodNode);
        methodNode.setInputArguments(sqrtMethod.getInputArguments());
        methodNode.setOutputArguments(sqrtMethod.getOutputArguments());
        methodNode.setInvocationHandler(sqrtMethod);

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
                methodNode.getNodeId(),
                Identifiers.HasComponent,
                folderNode.getNodeId().expanded(),
                false));
    }


    private void addGenerateEventMethod(UaFolderNode folderNode) {
        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
                .setNodeId(newNodeId("HelloWorld/generateEvent(eventTypeId)"))
                .setBrowseName(newQualifiedName("generateEvent(eventTypeId)"))
                .setDisplayName(new LocalizedText(null, "generateEvent(eventTypeId)"))
                .setDescription(
                        LocalizedText.english("Generate an Event with the TypeDefinition indicated by eventTypeId."))
                .build();

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
                methodNode.getNodeId(),
                Identifiers.HasComponent,
                folderNode.getNodeId().expanded(),
                false));
    }


    private void addCustomObjectTypeAndInstance(UaFolderNode rootFolder) {
        // Define a new ObjectType called "MyObjectType".
        UaObjectTypeNode objectTypeNode = UaObjectTypeNode.builder(getNodeContext())
                .setNodeId(newNodeId("ObjectTypes/MyObjectType"))
                .setBrowseName(newQualifiedName("MyObjectType"))
                .setDisplayName(LocalizedText.english("MyObjectType"))
                .setIsAbstract(false)
                .build();

        // "Foo" and "Bar" are members. These nodes are what are called "instance declarations" by the spec.
        UaVariableNode foo = UaVariableNode.builder(getNodeContext())
                .setNodeId(newNodeId("ObjectTypes/MyObjectType.Foo"))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName("Foo"))
                .setDisplayName(LocalizedText.english("Foo"))
                .setDataType(Identifiers.Int16)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        foo.addReference(new Reference(
                foo.getNodeId(),
                Identifiers.HasModellingRule,
                Identifiers.ModellingRule_Mandatory.expanded(),
                true));

        foo.setValue(new DataValue(new Variant(0)));
        objectTypeNode.addComponent(foo);

        UaVariableNode bar = UaVariableNode.builder(getNodeContext())
                .setNodeId(newNodeId("ObjectTypes/MyObjectType.Bar"))
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setBrowseName(newQualifiedName("Bar"))
                .setDisplayName(LocalizedText.english("Bar"))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        bar.addReference(new Reference(
                bar.getNodeId(),
                Identifiers.HasModellingRule,
                Identifiers.ModellingRule_Mandatory.expanded(),
                true));

        bar.setValue(new DataValue(new Variant("bar")));
        objectTypeNode.addComponent(bar);

        // Tell the ObjectTypeManager about our new type.
        // This let's us use NodeFactory to instantiate instances of the type.
        getServer().getObjectTypeManager().registerObjectType(
                objectTypeNode.getNodeId(),
                UaObjectNode.class,
                UaObjectNode::new);

        // Add the inverse SubtypeOf relationship.
        objectTypeNode.addReference(new Reference(
                objectTypeNode.getNodeId(),
                Identifiers.HasSubtype,
                Identifiers.BaseObjectType.expanded(),
                false));

        // Add type definition and declarations to address space.
        getNodeManager().addNode(objectTypeNode);
        getNodeManager().addNode(foo);
        getNodeManager().addNode(bar);

        // Use NodeFactory to create instance of MyObjectType called "MyObject".
        // NodeFactory takes care of recursively instantiating MyObject member nodes
        // as well as adding all nodes to the address space.
        try {
            UaObjectNode myObject = (UaObjectNode) getNodeFactory().createNode(
                    newNodeId("HelloWorld/MyObject"),
                    objectTypeNode.getNodeId());
            myObject.setBrowseName(newQualifiedName("MyObject"));
            myObject.setDisplayName(LocalizedText.english("MyObject"));

            // Add forward and inverse references from the root folder.
            rootFolder.addOrganizes(myObject);

            myObject.addReference(new Reference(
                    myObject.getNodeId(),
                    Identifiers.Organizes,
                    rootFolder.getNodeId().expanded(),
                    false));
        }
        catch (UaException e) {
            LOGGER.error("Error creating MyObjectType instance: {}", e.getMessage(), e);
        }
    }


    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }


    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }


    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }


    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

}
