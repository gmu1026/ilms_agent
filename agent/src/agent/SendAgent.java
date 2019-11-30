package agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendAgent {
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private final static String PROP_PATH = "resources/application.properties";
	private static Map<String, String> computer = new HashMap<String, String>();
	private static Properties properties;
	
	OkHttpClient client = new OkHttpClient();

	static {
		File file = new File("C:" + File.separator + "Agent");
		if (!file.exists()) {
			file.mkdir();
		}
		
		properties = new Properties();
		try {
			InputStream inputStream = SendAgent.class.getClassLoader().getResourceAsStream(PROP_PATH);
			properties.load(inputStream);
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadProp(String path) throws IOException {
		InputStream input = getClass().getResourceAsStream(PROP_PATH);
		properties.load(input);
		input.close();
	}
	
	public String post(String url, String json) throws IOException {
		RequestBody body = RequestBody.create(json, JSON);
		Request request = new Request.Builder().url(url).post(body).build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

	public void createHardWareInfo() {
		Process process = null;
		Runtime runtime = Runtime.getRuntime();
		
		String command = properties.getProperty("HARDWARE_INFO_COMMAND");
		try {
			process = runtime.exec(command);
			
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getHardWareInfo() {
		BufferedReader infoReader = null;
		
		try {
			infoReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(properties.getProperty("INFO_FILE_PATH"))), "EUC-KR"));
			
			boolean isStorage = false;
			boolean isProcessor = false;
			boolean isMemory = false;
			boolean isName = false;
			
			String storage = "";
			String msg = null;
			while ((msg = infoReader.readLine()) != null) {
				if (isStorage) {
					isStorage = false;
					if ("".equals(storage)) {
						storage += msg.trim().split(":\\s")[1];
					} else {
						storage += ", " + msg.trim().split(":\\s")[1];
					}
				}

				if (isName) {
					isName = false;
					boolean isNumber = false;
					computer.put("name", msg.trim().split(":\\s")[1]);
					
					try {
						Integer.parseInt(msg.trim().split(":\\s")[1].split("-")[0]);
						isNumber = true;
					} catch (Exception e) {
					}
					
					if (isNumber) {
						computer.put("laboratoryNo", msg.trim().split(":\\s")[1].split("-")[0]);
					} else {
						computer.put("laboratoryNo", "701");
					}
				}
				
				if (isMemory) {
					isMemory = false;
					computer.put("ram", msg.trim().split(":\\s")[1].split(" ")[0]);
				}

				if (isProcessor) {
					isProcessor = false;
					isMemory = true;
					computer.put("cpu", msg.trim().split(":\\s")[1]);
				}

				if (msg.indexOf("BIOS:") > -1) {
					isProcessor = true;
				}

				if (msg.indexOf("Card name:") > -1) {
					computer.put("vga", msg.trim().split(":\\s")[1]);
				}

				if (msg.indexOf("File System:") > -1) {
					isStorage = true;
				}
				
				if (msg.indexOf("Time of this report:") > -1) {
					isName = true;
				}
			}
			computer.put("storage", storage);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (infoReader != null) {
					infoReader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void getNetworkInfo() {
		Process process = null;
		Runtime runtime = Runtime.getRuntime();

		BufferedReader successReader = null;

		String msg = null;

		String command = properties.getProperty("NETWORK_INFO_COMMAND");
		try {
			process = runtime.exec(command);

			successReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR"));

			process.waitFor();

			while ((msg = successReader.readLine()) != null) {
				if (msg.indexOf("IPv4 주소") > -1) {
					String[] ip = msg.split("\\s:\\s");
					computer.put("ipAddress", ip[1].substring(0, ip[1].indexOf("(")));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				process.destroy();
				if (successReader != null) {
					successReader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Gson gson = new Gson();

		SendAgent sendAgent = new SendAgent();
		WatchFolder watchTest = new WatchFolder();
		
		if (watchTest.watchInfoFile()) {
			sendAgent.getHardWareInfo();
		}

		sendAgent.getNetworkInfo();
		
		String json = gson.toJson(computer);
		try {
			sendAgent.post("http://" + properties.getProperty("SERVER_IP") + "/computer/reg", json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
