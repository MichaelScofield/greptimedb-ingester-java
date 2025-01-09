package io.greptime;

import io.greptime.export.CompressionType;
import io.greptime.export.DataExportGrpc;
import io.greptime.export.DataExportRequest;
import io.greptime.export.DataExportResponse;
import io.greptime.models.*;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CanFrameIngester {

    /*
    create table can_frame (
      seqnum uint64,
      `version` uint8,
      countnum uint64,
      `timestamp` timestamp(6) time index,
      bus uint8,
      chn uint32,
      frameid uint32,
      len uint8,
      payload string,
    ) with (
      'append_mode' = 'true',
    );
    */
    @Metric(name = "can_frame")
    static class CanFrame {
        @Column(name = "seqnum", dataType = DataType.UInt64)
        long seqnum;

        @Column(name = "version", dataType = DataType.UInt8)
        int version;

        @Column(name = "countnum", dataType = DataType.UInt64)
        long countnum;

        @Column(name = "timestamp", timestamp = true, dataType = DataType.TimestampMicrosecond)
        long timestamp;

        @Column(name = "bus", dataType = DataType.UInt8)
        int bus;

        @Column(name = "chn", dataType = DataType.UInt32)
        int chn;

        @Column(name = "frameid", dataType = DataType.UInt32)
        int frameid;

        @Column(name = "len", dataType = DataType.UInt8)
        int len;

        @Column(name = "payload", dataType = DataType.String)
        String payload;
    }

    static class DataExporter {

        ManagedChannel channel = NettyChannelBuilder
                .forAddress(new DomainSocketAddress("/home/greptime/datahome/grpc.sock"))
                .eventLoopGroup(new EpollEventLoopGroup())
                .channelType(EpollDomainSocketChannel.class)
                .usePlaintext()
                .build();

        void export() {
            DataExportGrpc.DataExportBlockingStub stub = DataExportGrpc.newBlockingStub(channel);
            DataExportRequest request = DataExportRequest.newBuilder()
                    .addTables("can_frame")
                    .setFilePath("/home/greptime/export")
                    .setFileName(UUID.randomUUID().toString())
                    .setCompressionType(CompressionType.Zstd)
                    .setCompressionLevel(3)
                    .build();
            DataExportResponse response = stub.dataExport(request);
            System.out.println(response);
        }
    }

    public static void main(String[] args) throws Exception {
        // file format:
        // timestamp microsecond <space> frameid <space> len <space> payload in hex string <space> channel
        // each line is a can frame
        //
        // head -n3:
        // 1660503551173550 551 8 b5042d0043000081 1
        // 1660503551173750 583 8 550fff0d00040200 1
        // 1660503551174000 723 8 2901cf01ffb00034 1
        String file = "/home/greptime/can_bus_data";

        GreptimeDB db = TestConnector.connectToDefaultDB();

        List<String> lines = Files.readAllLines(Paths.get(file));

        // batch write CAN frames
        int batch = 10000;
        List<CanFrame> canFrames = new ArrayList<>(batch);

        long start = System.currentTimeMillis();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(" ");

            CanFrame frame = new CanFrame();
            frame.seqnum = i;
            frame.version = 1;
            frame.countnum = i;
            frame.timestamp = Long.parseLong(parts[0]);
            frame.bus = 3;
            frame.chn = Integer.parseInt(parts[4]);
            frame.frameid = Integer.parseInt(parts[1]);
            frame.len = Integer.parseInt(parts[2]);
            frame.payload = parts[3];
            canFrames.add(frame);

            if (canFrames.size() == batch) {
                Result<WriteOk, Err> result = db.writeObjects(canFrames).get();
                if (!result.isOk()) {
                    throw new RuntimeException(result.getErr().toString());
                }
                canFrames.clear();
            }
        }
        if (!canFrames.isEmpty()) {
            Result<WriteOk, Err> result = db.writeObjects(canFrames).get();
            if (!result.isOk()) {
                throw new RuntimeException(result.getErr().toString());
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed: " + (elapsed / 1000.0) + "s");
        System.out.println("Total ingested CAN frames: " + lines.size());

        db.shutdownGracefully();

        DataExporter exporter = new DataExporter();
        exporter.export();
    }
}
