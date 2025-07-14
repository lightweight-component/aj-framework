//package com.ajaxjs.business.batch_read;
//
//import com.ajaxjs.batch_read.util.GuidCreator;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
//import org.apache.poi.openxml4j.opc.OPCPackage;
//import org.apache.poi.openxml4j.opc.PackageAccess;
//
//import org.xml.sax.SAXException;
//
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.File;
//import java.io.IOException;
//import java.util.Date;
//
//public class TestBatchRead {
//    @Test
//    public void test() {
//        // final String fileName = "d:/log_small.csv";
//        final String fileName = "d:/test_big.xlsx";
//
//        try {
//            GuidCreator myGUID = new GuidCreator();
//            BatchDTO taskContext = new BatchDTO();
//            String batchId = myGUID.createNewGuid(GuidCreator.AfterMD5);
//            taskContext.setPkBtTaskId(batchId);
//            taskContext.setTaskName(BatchTask.TXT_IMP_EXP);
//            taskContext.setTaskDesc(fileName);
//            taskContext.setCommitedBy("unittest");
//            taskContext.setStatus(BatchTask.TASK_RUNNING);
//            taskContext.setCommitedTime(new Date());
//            taskContext.setBatchId(batchId);
//            taskContext.setHeadSkip(true);
//            // BatchImportExec task = new BatchImportExec(
//            // Constants.ENUMERATION_TXT_TASK, fileName, "", taskContext);
////			task.doBatch();
//            // if (data != null && data.size() > 0) {
//            // for (int i = 0; i < data.size(); i++) {
//            // System.out.println("rows: " + i + "=====" + data.get(i));
//            // }
//            // }
////			BatchImportExec task = new BatchImportExec(Constants.ENUMERATION_EXCEL_TASK, fileName, "", taskContext);
////			task.doBatch();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Test
//    public void testXLSXParser() throws OpenXML4JException, IOException, ParserConfigurationException, SAXException {
//        /*
//         * if (args.length < 1) { System.err.println("Use:");
//         * System.err.println("  XLSX2CSV <xlsx file> [min columns]"); return; }
//         */
//
//        // File xlsxFile = new File(args[0]);
//        File xlsxFile = new File("d:/test.xlsx");
//        if (!xlsxFile.exists()) {
//            System.err.println("Not found or not a file: " + xlsxFile.getPath());
//            return;
//        }
//
//        int minColumns = -1;
//        // if (args.length >= 2)
//        // minColumns = Integer.parseInt(args[1]);
//
//        minColumns = 2;
//        // The package open is instantaneous, as it should be.
//        OPCPackage p = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);
//        XLSXParser xlsxParser = new XLSXParser(p, null, false);
//        xlsxParser.process();
//    }
//}
