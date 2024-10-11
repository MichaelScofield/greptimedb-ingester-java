package io.greptime;

import org.apache.arrow.adbc.core.AdbcConnection;
import org.apache.arrow.adbc.core.AdbcDatabase;
import org.apache.arrow.adbc.core.AdbcDriver;
import org.apache.arrow.adbc.core.AdbcStatement;
import org.apache.arrow.adbc.driver.jdbc.JdbcDriver;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;

import java.util.HashMap;
import java.util.Map;

public class AdbcExample {
    public static void main(String[] args) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AdbcDriver.PARAM_URL, "jdbc:mysql://localhost:4002/public");

        try (
                BufferAllocator allocator = new RootAllocator();
                AdbcDatabase db = new JdbcDriver(allocator).open(parameters);
                AdbcConnection adbcConnection = db.connect();
                AdbcStatement stmt = adbcConnection.createStatement()
        ) {
            stmt.setSqlQuery("select * from foo");
            AdbcStatement.QueryResult queryResult = stmt.executeQuery();
            while (queryResult.getReader().loadNextBatch()) {
                VectorSchemaRoot root = queryResult.getReader().getVectorSchemaRoot();
                System.out.println(root.contentToTSVString());
            }
        }
    }
}
