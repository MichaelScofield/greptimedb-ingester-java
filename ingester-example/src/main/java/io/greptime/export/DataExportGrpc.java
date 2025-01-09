package io.greptime.export;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.69.0)",
    comments = "Source: export.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DataExportGrpc {

  private DataExportGrpc() {}

  public static final java.lang.String SERVICE_NAME = "DataExport";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.greptime.export.DataExportRequest,
      io.greptime.export.DataExportResponse> getDataExportMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DataExport",
      requestType = io.greptime.export.DataExportRequest.class,
      responseType = io.greptime.export.DataExportResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.greptime.export.DataExportRequest,
      io.greptime.export.DataExportResponse> getDataExportMethod() {
    io.grpc.MethodDescriptor<io.greptime.export.DataExportRequest, io.greptime.export.DataExportResponse> getDataExportMethod;
    if ((getDataExportMethod = DataExportGrpc.getDataExportMethod) == null) {
      synchronized (DataExportGrpc.class) {
        if ((getDataExportMethod = DataExportGrpc.getDataExportMethod) == null) {
          DataExportGrpc.getDataExportMethod = getDataExportMethod =
              io.grpc.MethodDescriptor.<io.greptime.export.DataExportRequest, io.greptime.export.DataExportResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DataExport"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.greptime.export.DataExportRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.greptime.export.DataExportResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataExportMethodDescriptorSupplier("DataExport"))
              .build();
        }
      }
    }
    return getDataExportMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DataExportStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataExportStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataExportStub>() {
        @java.lang.Override
        public DataExportStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataExportStub(channel, callOptions);
        }
      };
    return DataExportStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DataExportBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataExportBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataExportBlockingStub>() {
        @java.lang.Override
        public DataExportBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataExportBlockingStub(channel, callOptions);
        }
      };
    return DataExportBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DataExportFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataExportFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataExportFutureStub>() {
        @java.lang.Override
        public DataExportFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataExportFutureStub(channel, callOptions);
        }
      };
    return DataExportFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void dataExport(io.greptime.export.DataExportRequest request,
        io.grpc.stub.StreamObserver<io.greptime.export.DataExportResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDataExportMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DataExport.
   */
  public static abstract class DataExportImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DataExportGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DataExport.
   */
  public static final class DataExportStub
      extends io.grpc.stub.AbstractAsyncStub<DataExportStub> {
    private DataExportStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataExportStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataExportStub(channel, callOptions);
    }

    /**
     */
    public void dataExport(io.greptime.export.DataExportRequest request,
        io.grpc.stub.StreamObserver<io.greptime.export.DataExportResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDataExportMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DataExport.
   */
  public static final class DataExportBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DataExportBlockingStub> {
    private DataExportBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataExportBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataExportBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.greptime.export.DataExportResponse dataExport(io.greptime.export.DataExportRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDataExportMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DataExport.
   */
  public static final class DataExportFutureStub
      extends io.grpc.stub.AbstractFutureStub<DataExportFutureStub> {
    private DataExportFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataExportFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataExportFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.greptime.export.DataExportResponse> dataExport(
        io.greptime.export.DataExportRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDataExportMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_DATA_EXPORT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DATA_EXPORT:
          serviceImpl.dataExport((io.greptime.export.DataExportRequest) request,
              (io.grpc.stub.StreamObserver<io.greptime.export.DataExportResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getDataExportMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.greptime.export.DataExportRequest,
              io.greptime.export.DataExportResponse>(
                service, METHODID_DATA_EXPORT)))
        .build();
  }

  private static abstract class DataExportBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DataExportBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.greptime.export.Export.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DataExport");
    }
  }

  private static final class DataExportFileDescriptorSupplier
      extends DataExportBaseDescriptorSupplier {
    DataExportFileDescriptorSupplier() {}
  }

  private static final class DataExportMethodDescriptorSupplier
      extends DataExportBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    DataExportMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DataExportGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DataExportFileDescriptorSupplier())
              .addMethod(getDataExportMethod())
              .build();
        }
      }
    }
    return result;
  }
}
