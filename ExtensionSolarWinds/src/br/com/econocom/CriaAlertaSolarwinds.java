package br.com.econocom;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class CriaAlertaSolarwinds {

	private static String Solarwinds;
	private static String UserSolarwinds;
	private static String PassSolarwinds;
	private static String AlertObject;

	final static Logger logger = Logger.getLogger(CriaAlertaSolarwinds.class);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*
		 * args[0] = Application
		 * args[10] = BT
		 * args[4] = Tipo (ErroR)
		 * args[26] = Descricao
		 * args[27] = IncidentID
		 * args[28] = Deeplink
		 */

		PropertyConfigurator.configure("log4j.properties");

		//if(logger.isDebugEnabled()){
		logger.debug("Parametros");
		for (int x=0;x<args.length;x++) {
			logger.debug("Args[" + Integer.toString(x) + "]=" + args[x]);
		}
		//}

		if (args[0].contains("password")) {
			try {
				String plainText = args[1];
				System.out.println("Plain Text Before Encryption: " + plainText);

				String encryptedText = encrypt(plainText);
				System.out.println("Encrypted Text After Encryption: " + encryptedText);
				System.exit(0);
			} catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Erro ao encriptar a senha=" + e);
				System.exit(1);
			}
		}

		logger.info("Carrega properties");
		getProperties();


		/* SolarWinds */
		String strseverity = "1";
		if (args[4].contains("ERROR")) strseverity = "2";
		if (args[4].contains("INFO")) strseverity = "1";

		String query = "";
		String strAlertID = "";
		String strAlertObjectID = "";

		String input = "[\"SELECT AlertID FROM AlertConfigurations WHERE Name = 'AppDynamics'\"]";
		logger.info("input=" + input);
		String texto = RESTSolarWinds(input, "AlertID");

		if (texto.contains("ERRO")) { System.exit(1); }

		if (texto.contains("{\"results\":[]}")) {
			logger.info("Insere Alerta Configurations");
			query = "INSERT INTO AlertConfigurations (AlertMessage,AlertRefID,Name,Description,Enabled,ObjectType,Frequency,BlockUntil,[Trigger],Reset,Severity,NotifyEnabled,NotificationSettings,LastEdit,CreatedBy,Category,Canned) ";
			query += "SELECT TOP 1 AlertMessage,NEWID(),'AppDynamics',Description,Enabled,ObjectType,Frequency,BlockUntil,[Trigger],Reset,Severity,NotifyEnabled,NotificationSettings,GETDATE(),CreatedBy,Category,Canned  FROM AlertConfigurations WHERE Name = 'Alert me when an application goes down' and ObjectType = 'APM: Application'";

			input = "[\""+ query + "\"]";
			texto = RESTSolarWinds(input, "");

			if (texto.contains("ERRO")) { System.exit(1); }

			input = "[\"SELECT AlertID FROM AlertConfigurations WHERE AlertMessage = '"+args[0].replace("\"", "") + "." + args[10].replace("\"", "") + "' and severity = " + strseverity + "\"]";
			texto = RESTSolarWinds(input, "AlertID");

			if (texto.contains("ERRO")) { System.exit(1); }
		}

		
		
		input = "[\"SELECT AlertID FROM AlertConfigurations WHERE AlertMessage = '"+args[0].replace("\"", "") + "." + args[10].replace("\"", "")+ "' and severity = " + strseverity + "\"]";
		logger.info("input=" + input);
		texto = RESTSolarWinds(input, "AlertID");

		if (texto.contains("ERRO")) { System.exit(1); }

		if (texto.contains("{\"results\":[]}")) {
			logger.info("Insere Alerta Configurations");
			query = "INSERT INTO AlertConfigurations (AlertMessage,AlertRefID,Name,Description,Enabled,ObjectType,Frequency,BlockUntil,[Trigger],Reset,Severity,NotifyEnabled,NotificationSettings,LastEdit,CreatedBy,Category,Canned) ";
			query += "SELECT '" + args[0].replace("\"", "") + "." + args[10].replace("\"", "") + "',NEWID(),'" + args[0].replace("\"", "") + "." + args[10].replace("\"", "") + "','" + args[0].replace("\"", "") + "." + args[10].replace("\"", "") + "'";
			query += ",0,'APM: Component',60,'1900-01-01 00:00:00.000', [TRIGGER] , [Reset] ";
			query += ","+ strseverity + ",1,null,GETUTCDATE(),'admin','',0 FROM AlertConfigurations WHERE Name = 'AppDynamics'";

			input = "[\""+ query + "\"]";
			texto = RESTSolarWinds(input, "");

			if (texto.contains("ERRO")) { System.exit(1); }

			input = "[\"SELECT AlertID FROM AlertConfigurations WHERE AlertMessage = '"+args[0].replace("\"", "") + "." + args[10].replace("\"", "") + "' and severity = " + strseverity + "\"]";
			texto = RESTSolarWinds(input, "AlertID");

			if (texto.contains("ERRO")) { System.exit(1); }
		}

		strAlertID = texto;
		logger.info("Insere AlertObjects");
		query = "INSERT INTO AlertObjects (AlertID,EntityUri,EntityType,EntityCaption,EntityDetailsUrl,EntityNetObjectId,RelatedNodeCaption,RelatedNodeUri,RelatedNodeId,RelatedNodeDetailsUrl,RealEntityUri,RealEntityType,TriggeredCount";
		query += ",LastTriggeredDateTime,Context,AlertNote)";
		query += "SELECT Top 1 " + strAlertID + ",EntityUri,EntityType,EntityCaption,EntityDetailsUrl,EntityNetObjectId,RelatedNodeCaption,RelatedNodeUri,RelatedNodeId,RelatedNodeDetailsUrl,RealEntityUri,RealEntityType";
		query += ",0,null,Context,AlertNote FROM SolarWindsOrion.dbo.AlertObjects where EntityCaption = '"+ AlertObject + "' order by alertid desc";

		input = "[\""+ query + "\"]";
		texto = RESTSolarWinds(input, "");

		if (texto.contains("ERRO")) { System.exit(1); }

		query = "SELECT TOP 1 AlertObjectID FROM AlertObjects WHERE EntityCaption = '"+ AlertObject + "' order by alertid desc";
		input = "[\""+ query + "\"]";
		texto = RESTSolarWinds(input, "AlertObjectID");

		if (texto.contains("ERRO")) { System.exit(1); }

		strAlertObjectID = texto;
		logger.info("Cria Alerta");
		query = "INSERT INTO [AlertActive] (AlertObjectID, TriggeredDateTime, TriggeredMessage, Acknowledged, AcknowledgedBy, AcknowledgedDateTime)";
		if (args.length > 20 ) {
			query += "VALUES(" + strAlertObjectID +", GETUTCDATE(), '"+args[26].replace("\"", "") + " - Link: " + args[28].replace("\"", "") + args[27].replace("\"", "") + "', null, null, null)";
		} else
		{
			query += "VALUES(" + strAlertObjectID +", GETUTCDATE(), '"+args[17].replace("\"", "") + " - Link: " + args[18].replace("\"", "") + "', null, null, null)";
		}
		input = "[\""+ query + "\"]";
		texto = RESTSolarWinds(input, "");

		if (texto.contains("ERRO")) { System.exit(1); }

		/*
		//creating connection to Oracle database using JDBC
		Connection conn;
		try {
			logger.info("Inicia Conexão do Banco de Dados");
			//conn = DriverManager.getConnection("jdbc:sqlserver://"+ BDSolawinds + ":1433;database=SolarWindsOrion;user="+UserDBSolarwinds+";password="+PassDBSolarwinds );
			conn = DriverManager.getConnection("jdbc:sqlserver://192.168.56.71:1433;database=SolarWindsOrion;user="+UserDBSolarwinds+";password=@syrix123");
			Statement stmt = conn.createStatement();

			logger.info("Verifica se existe o registro");
			String query = "SELECT AlertID FROM AlertConfigurations WHERE AlertMessage = '"+args[0] + "." + args[10]+ "'";
			if(logger.isDebugEnabled()){ logger.debug("Query=" + query); }
			ResultSet rs = stmt.executeQuery(query);


			if (!rs.next() )  {
				logger.info("Insere Alerta Configurations");
				query = "INSERT INTO AlertConfigurations (AlertMessage,AlertRefID,Name,Description,Enabled,ObjectType,Frequency,BlockUntil,[Trigger],Reset,Severity,NotifyEnabled,NotificationSettings,LastEdit,CreatedBy,Category,Canned) VALUES";
				query += "('" + args[0] + "." + args[10] + "',NEWID(),'" + args[0] + "." + args[10] + "','" + args[0] + "." + args[10] + "'";
				query += ",0,'APM: Component',60,'1900-01-01 00:00:00.000','<ArrayOfAlertConditionShelve xmlns=\"http://schemas.datacontract.org/2004/07/SolarWinds.Orion.Core.Models.Alerting\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><AlertConditionShelve><AndThenTimeInterval i:nil=\"true\"/><ChainType>Trigger</ChainType><ConditionTypeID>Core.Dynamic</ConditionTypeID><Configuration>&lt;AlertConditionDynamic xmlns=\"http://schemas.datacontract.org/2004/07/SolarWinds.Orion.Core.Alerting.Plugins.Conditions.Dynamic\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;&lt;ExprTree xmlns:a=\"http://schemas.datacontract.org/2004/07/SolarWinds.Orion.Core.Models.Alerting\"&gt;&lt;a:Child&gt;&lt;a:Expr&gt;&lt;a:Child&gt;&lt;a:Expr&gt;&lt;a:Child/&gt;&lt;a:NodeType&gt;Field&lt;/a:NodeType&gt;&lt;a:Value&gt;Orion.APM.Component|Status&lt;/a:Value&gt;&lt;/a:Expr&gt;&lt;/a:Child&gt;&lt;a:NodeType&gt;Operator&lt;/a:NodeType&gt;&lt;a:Value&gt;ISNOTNULL&lt;/a:Value&gt;&lt;/a:Expr&gt;&lt;/a:Child&gt;&lt;a:NodeType&gt;Operator&lt;/a:NodeType&gt;&lt;a:Value&gt;AND&lt;/a:Value&gt;&lt;/ExprTree&gt;&lt;Scope i:nil=\"true\" xmlns:a=\"http://schemas.datacontract.org/2004/07/SolarWinds.Orion.Core.Models.Alerting\"/&gt;&lt;TimeWindow i:nil=\"true\"/&gt;&lt;/AlertConditionDynamic&gt;</Configuration><ConjunctionOperator>None</ConjunctionOperator><IsInvertedMinCountThreshold>false</IsInvertedMinCountThreshold><NetObjectsMinCountThreshold i:nil=\"true\"/><ObjectType>APM: Component</ObjectType><SustainTime i:nil=\"true\"/></AlertConditionShelve></ArrayOfAlertConditionShelve>','<ArrayOfAlertConditionShelve xmlns=\"http://schemas.datacontract.org/2004/07/SolarWinds.Orion.Core.Models.Alerting\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><AlertConditionShelve><AndThenTimeInterval i:nil=\"true\"/><ChainType>NoReset</ChainType><ConditionTypeID i:nil=\"true\"/><Configuration i:nil=\"true\"/><ConjunctionOperator>None</ConjunctionOperator><IsInvertedMinCountThreshold>false</IsInvertedMinCountThreshold><NetObjectsMinCountThreshold i:nil=\"true\"/><ObjectType i:nil=\"true\"/><SustainTime i:nil=\"true\"/></AlertConditionShelve></ArrayOfAlertConditionShelve>'";
				query += ",2,1,null,GETUTCDATE(),'admin','',0)";

				// Criando a instrução a partir do SELECT que está dentro da variável query
				if(logger.isDebugEnabled()){ logger.debug("Query=" + query); }
				stmt.executeUpdate(query);
			}

			query = "SELECT AlertID FROM AlertConfigurations WHERE AlertMessage = '"+args[0] + "." + args[10]+ "'";
			if(logger.isDebugEnabled()){ logger.debug("Query=" + query); }
			rs = stmt.executeQuery(query);

			String strAlertID = "";
			while (rs.next()) {
				strAlertID =rs.getString("AlertID");
			}

			logger.info("Insere AlertObjects");
			query = "INSERT INTO AlertObjects (AlertID,EntityUri,EntityType,EntityCaption,EntityDetailsUrl,EntityNetObjectId,RelatedNodeCaption,RelatedNodeUri,RelatedNodeId,RelatedNodeDetailsUrl,RealEntityUri,RealEntityType,TriggeredCount";
			query += ",LastTriggeredDateTime,Context,AlertNote)";
			query += "SELECT Top 1 " + strAlertID + ",EntityUri,EntityType,EntityCaption,EntityDetailsUrl,EntityNetObjectId,RelatedNodeCaption,RelatedNodeUri,RelatedNodeId,RelatedNodeDetailsUrl,RealEntityUri,RealEntityType";
			query += ",0,null,Context,AlertNote FROM SolarWindsOrion.dbo.AlertObjects where EntityCaption = '"+ AlertObject + "' order by alertid desc";
			if(logger.isDebugEnabled()){ logger.debug("Query=" + query); }
			stmt.executeUpdate(query);

			query = "SELECT TOP 1 AlertObjectID FROM AlertObjects WHERE EntityCaption = '"+ AlertObject + "' order by alertid desc";
			if(logger.isDebugEnabled()){ logger.debug("Query=" + query); }
			rs = stmt.executeQuery(query);
			String strAlertObjectID = "";
			while (rs.next()) {
				strAlertObjectID =rs.getString("AlertObjectID");
			}

			logger.info("Cria Alerta");
			query = "INSERT INTO [AlertActive] (AlertObjectID, TriggeredDateTime, TriggeredMessage, Acknowledged, AcknowledgedBy, AcknowledgedDateTime)";
			query += "VALUES(" + strAlertObjectID +", GETUTCDATE(), '"+args[26] + " - Link: " + args[28] + "', null, null, null)";
			if(logger.isDebugEnabled()){ logger.debug("Query=" + query); }
			stmt.executeUpdate(query);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Erro de SQL=" + e);
		}
		 */
	}

	private static void getProperties() {

		Properties prop = new Properties();
		InputStream input = null;

		try {
			if(logger.isDebugEnabled()){ logger.debug("Valores config.properties"); }
			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			Solarwinds = prop.getProperty("Solawinds");
			UserSolarwinds = prop.getProperty("UserSolarwinds");
			PassSolarwinds = prop.getProperty("PassSolarwinds");
			AlertObject = prop.getProperty("AlertObject");

			if(logger.isDebugEnabled()) { 
				logger.debug("Solawinds="+ Solarwinds);
				logger.debug("UserSolarwinds="+ UserSolarwinds);
				logger.debug("PassSolarwinds="+ PassSolarwinds);
				logger.debug("AlertObject="+ AlertObject);
			}	

			String decryptedText = decrypt(PassSolarwinds);
			PassSolarwinds = decryptedText;

		} catch (IOException io) {
			io.printStackTrace();
			logger.error("Arquivo properties não encontrado.");
		} catch(Exception e) {
			logger.error(e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("Erro ao fechar o arquivo properties.");
				}
			}

		}

	}

	public static String RESTSolarWinds(String input, String campo) {
		URL someUrl;
		String texto = "";
		try {
			someUrl = new URL("https://" + Solarwinds + ":17778/SolarWinds/InformationService/v3/Json/Invoke/Orion.Reporting/ExecuteSQL");

			HttpURLConnection connection = (HttpURLConnection) someUrl.openConnection();

			TrustModifier.relaxHostChecking(connection); // here's where the magic happens
			String userpass = UserSolarwinds + ":" + PassSolarwinds;
			//String userpass = "admin:";
			String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
			connection.setRequestProperty ("Authorization", basicAuth);
			connection.setDoOutput(true);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStream os = connection.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ connection.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(connection.getInputStream())));

			logger.debug("Output from Server .... \n");
			String output;

			while ((output = br.readLine()) != null) {
				texto += output;
				logger.debug(output);
			}
			connection.disconnect();

			if (!campo.isEmpty()) {
				JSONParser parser = new JSONParser();
				Object obj;
				try {
					obj = parser.parse(texto);
					JSONObject jsonObject = (JSONObject) obj;
					JSONArray msg = (JSONArray) jsonObject.get("results");
					Iterator<JSONObject> iterator = msg.iterator();
					while (iterator.hasNext()) {
						JSONObject factObj = (JSONObject) iterator.next();
						String water = factObj.get(campo).toString();
						//System.out.println(water);
						texto = water;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e);

				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			texto = "ERRO";
			logger.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			texto = "ERRO";
			logger.error(e);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			texto = "ERRO";
			logger.error(e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			texto = "ERRO";
			logger.error(e);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			texto = "ERRO";
			logger.error(e);
		}
		return texto;
	}

	public static String encrypt(String plainText)
			throws Exception {
		String encryptedText = new String(Base64.encodeBase64((plainText).getBytes()));	
		return encryptedText;

	}

	public static String decrypt(String encryptedText)
			throws Exception {
		String decryptedText = new String(Base64.decodeBase64((encryptedText).getBytes()));	
		return decryptedText;
	}

}
