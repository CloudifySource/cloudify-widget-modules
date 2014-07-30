package cloudify.widget.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.io.*;
import java.util.*;

/**
 * Created by sefi on 7/23/14.
 */
public class DatabaseBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBuilder.class);

    public static void destroyDatabase(JdbcTemplate jdbcTemplate, String schema) {
        jdbcTemplate.update("drop schema " + schema);
    }

    public static void buildDatabase(JdbcTemplate jdbcTemplate, String schema, String sqlClassPath) {

        jdbcTemplate.update("create schema " + schema);
        jdbcTemplate.update("use " + schema);

        // going through all files under the 'sql' folder, and executing all of them.
        Iterator<File> sqlFileIterator = org.apache.commons.io.FileUtils.iterateFiles(
                FileUtils.getFileInClasspath(sqlClassPath), new String[]{sqlClassPath}, false);

        List<File> orderedSqlFiles = new ArrayList<File>();
        while (sqlFileIterator.hasNext()) {
            File file = sqlFileIterator.next();
            orderedSqlFiles.add(file);
        }
        // sort the sql executions, relying on numbered file names
        Collections.sort(orderedSqlFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String o1NoExtension = _stripExtension(o1.getName());
                String o2NoExtension = _stripExtension(o2.getName());
                return Integer.parseInt(o1NoExtension) - Integer.parseInt(o2NoExtension);
            }

            private String _stripExtension(String name) {
                return name.substring(0, name.indexOf('.'));
            }
        });

        for (File sqlFile : orderedSqlFiles) {
            logger.info("executing statements in file [{}]", sqlFile.getName());
            List<String> statements = readSqlStatementsFromFile(sqlFile);
            logger.info("found statements [{}]", statements);
            for (String stmt : statements) {
                jdbcTemplate.update(stmt);
            }
        }
    }

    private static String readSqlStatementFromFile(File file) {

        String script = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            LineNumberReader fileReader = new LineNumberReader(in);
            script = JdbcTestUtils.readScript(fileReader);
        } catch (IOException e) {
            logger.error("failed to read sql script from file", e);
        }
        return script;
    }

    private static List<String> readSqlStatementsFromFile(File file) {
        String script = readSqlStatementFromFile(file);
        List<String> statements = new ArrayList<String>();
        JdbcTestUtils.splitSqlScript(script, ';', statements);
        return statements;
    }

}
