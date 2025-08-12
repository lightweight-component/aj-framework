package com.ajaxjs.business.datastru.smtp;

public class SMTP extends Thread {
    private static final Queue queue = new Queue();
    private static int nowHave;

    public static void showProgress() {
//        SpeedBar sb = new SpeedBar("待送郵件數目");
        while (queue.getSize() > 0) {
//            sb.setSpeed(queue.size, "待送郵件數目:");
            try {
                sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        sb.dispose();
    }

    /**
     * Send email to a group of users. The method will be called by all mail functions
     *
     * @param sender   Mail sender's email account
     * @param receiver Array of receivers' email account
     * @param subject  email subject
     * @param data     An array of String which constructs the content of the email
     */
    public static void send(String sender, String[] receiver, String subject, String[] data) {
        Request req = new Request();
        req.sender = sender;
        req.receiver = receiver;
        req.subject = subject;
        req.data = data;
        queue.enQueue(req);

        while (nowHave < 5) {
            (new SMTP()).start();
            nowHave++;
        }
    }

    public void run() {
        for (; ; ) {
            Request tmp = queue.deQueue();
            String sender = tmp.sender;
            String[] receiver = tmp.receiver;
            String subject = tmp.subject;
            String[] data = tmp.data;

            for (; ; ) {
                try {
//                    Socket smtp = new Socket(Environment.getMailServer(), 25);
//                    PrintStream smtpsOutput = new PrintStream(smtp.getOutputStream());
//                    BufferedReader smtpsInput = new BufferedReader(new InputStreamReader(smtp.getInputStream()));
//                    smtpsInput.readLine();
//                    smtpsOutput.println("HELO " + Environment.getDomain());
//                    smtpsOutput.flush();
//                    smtpsInput.readLine();
//                    smtpsOutput.println("MAIL FROM:" + sender);  // 寄 信 人
//                    smtpsOutput.flush();
//                    smtpsInput.readLine();
////                    for (int i = 0; i "); smtpsOutput.flush(); smtpsOutput.print("To: ");
////                    for (int i = 0; i "); // 收 信 人
//
//
//                    smtpsOutput.println();
//                    smtpsOutput.flush();
//                    smtpsOutput.println("MIME-Version: 1.0");
//                    smtpsOutput.flush();
//                    smtpsOutput.println("Content-Type: text/plain;\n\tcharset=\"big5\"");
//                    smtpsOutput.flush();
//                    smtpsOutput.println("Content-Transfer-Encoding: 8bit");
//                    smtpsOutput.flush();
//                    smtpsOutput.print("Subject:");  // 標 題
//                    smtpsOutput.write(subject.getBytes("big5"));
//                    smtpsOutput.println();
//                    smtpsOutput.flush();
//                    smtpsOutput.println();
//                    smtpsOutput.flush();
//
//                    for (int i = 0; i0 && data[i].charAt(0) == '.'; ) {
//                        smtpsOutput.write('.');
//
//                        smtpsOutput.write(data[i].getBytes("big5"));
//                        smtpsOutput.println();
//                        smtpsOutput.flush();
//                    }
//
//                    smtpsOutput.println(".");  // 資料結束
//
//                    smtpsOutput.flush();
//                    smtpsInput.readLine();
//                    smtpsOutput.println("QUIT");  // 斷線
//                    smtpsOutput.close();
//                    smtpsInput.close();
//                    smtp.close();
//                    break;
                } catch (Exception e) {
                    if (queue.isFull()) {
                        System.out.println("drop mail " + sender + " " + subject);
                        break;
                    }
                }
            }
        }
    }
}