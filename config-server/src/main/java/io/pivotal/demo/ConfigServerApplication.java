package io.pivotal.demo;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);

	
	}
}
class JGitTest {
	
	public void test() {
		SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {

			@Override
			protected void configure(Host hc, Session session) {
				// TODO Auto-generated method stub

			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {

				JSch defaultJSch = super.createDefaultJSch(fs);

				defaultJSch.addIdentity("/Users/mrosales/.ssh/id_rsa");
				
				return defaultJSch;

			}

		};
		CloneCommand cloneCommand = Git.cloneRepository();

		cloneCommand.setURI("git@github.com:spring-cloud-samples/configserver.git");
		cloneCommand.setDirectory(new File("/Users/mrosales/Documents/solera-ws/tmp"));
		cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
			@Override
			public void configure(Transport transport) {
				SshTransport sshTransport = (SshTransport) transport;
				sshTransport.setSshSessionFactory(sshSessionFactory);
			}
		});

		try {
			cloneCommand.call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}