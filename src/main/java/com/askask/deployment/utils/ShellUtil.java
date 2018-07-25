package com.askask.deployment.utils;

import com.askask.deployment.dto.shell.ShellExecResult;
import com.jcraft.jsch.*;
import lombok.Builder;

import java.io.*;

@Builder
public class ShellUtil {
    private String host;
    private Integer port;
    private String username;
    private String privateKey;
    private Session session;
    @Builder.Default
    private boolean ptimestamp = false;

    private Channel openChannel(String type) throws JSchException {
        if (session == null || session.isConnected() == false) {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey);
            this.session = jsch.getSession(username, host, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
        }
        return session.openChannel(type);
    }

    public ShellExecResult scpTo(String localFIlePath, String remoteFilePath) {
        ShellExecResult shellExecResult = new ShellExecResult();
        FileInputStream fis = null;
        try {
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remoteFilePath;
            ChannelExec channel = (ChannelExec) this.openChannel("exec");
            channel.setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                shellExecResult.setExitStatus(1);
                shellExecResult.setResultData("checkAck is " + checkAck(in));
            }

            File _lfile = new File(localFIlePath);

            if (ptimestamp) {
                command = "T " + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    shellExecResult.setExitStatus(1);
                    shellExecResult.setResultData("checkAck is " + checkAck(in));
                }
            }
            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (localFIlePath.lastIndexOf('/') > 0) {
                command += localFIlePath.substring(localFIlePath.lastIndexOf('/') + 1);
            } else {
                command += localFIlePath;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                shellExecResult.setExitStatus(1);
                shellExecResult.setResultData("checkAck is " + checkAck(in));
            }

            // send a content of lfile
            fis = new FileInputStream(localFIlePath);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                shellExecResult.setExitStatus(1);
                shellExecResult.setResultData("checkAck is " + checkAck(in));
            }
            out.close();
            while (true) {
                if (channel.isClosed()) {
                    shellExecResult.setExitStatus(channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    System.out.println(ee);
                }
            }
            channel.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        return shellExecResult;
    }

    public ShellExecResult scpFrom(String remoteFilePath, String localFIlePath) {
        ShellExecResult shellExecResult = new ShellExecResult();
        FileOutputStream fos = null;
        try {
            String prefix = null;
            if (new File(localFIlePath).isDirectory()) {
                prefix = localFIlePath + File.separator;
            }

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + remoteFilePath;
            ChannelExec channel = (ChannelExec) this.openChannel("exec");
            channel.setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();
            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }
                // read '0644 '
                in.read(buf, 0, 5);
                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') break;
                    filesize = filesize * 10L + (long) (buf[0] - '0');

                }
                //System.out.println("filesize=" + filesize);

                String file = null;
                for (int i = 0; ; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }
                //System.out.println("file=" + file);

                //System.out.println("filesize=" + filesize + ", file=" + file);

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of lfile
                fos = new FileOutputStream(prefix == null ? localFIlePath : prefix + file);
                int foo;
                while (true) {
                    if (buf.length < filesize) foo = buf.length;
                    else foo = (int) filesize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L) break;
                }
                fos.close();
                fos = null;

                if (checkAck(in) != 0) {
                    System.exit(0);
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }
            while (true) {
                if (channel.isClosed()) {
                    shellExecResult.setExitStatus(channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    System.out.println(ee);
                }
            }
            channel.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shellExecResult;
    }


    public ShellExecResult exec(String script) {
        ShellExecResult shellExecResult = new ShellExecResult();
        try {
            ChannelExec channel = (ChannelExec) this.openChannel("exec");
            // sudo 可能遇到問題時用
            //channel.setPty(true);
            channel.setCommand(script);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.setErrStream(baos, true);
            channel.setOutputStream(baos, true);
            channel.connect();

            while (true) {
                if (channel.isClosed()) {
                    shellExecResult.setExitStatus(channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    System.out.println(ee);
                }
            }
            channel.disconnect();
            shellExecResult.setResultData(baos.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shellExecResult;
    }

    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public void disconnect() {
        if (this.session != null && this.session.isConnected()) {
            this.session.disconnect();
        }
    }
}
