/**
* Copyright 2015 HLRS, University of Stuttgart
* All rights reserved.
* Redistribution and use in source and binary forms, with or without modification,
* are permitted (subject to the limitations in the disclaimer below) provided that the
* following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of
* conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of
* conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

* Neither the name of [Owner Organization] nor the names of its contributors may be used
* to endorse or promote products derived from this software without specific prior written permission.

* NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE.
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
* OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
* AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
* ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
**/

package eu.juniper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MonitoringLib {

	protected int answer = 0;
	private String appId = "";
	private String filepath = "D:/";
	private String monitoringServiceURL = "";
	private String monitoringServiceMetricsURL = "";
	private String monitoringServiceDetailsURL = "";
	private String monitoringServiceMetricValues = "";
	private double iValue = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE, avg = 0, sum = 0;
	private int count = 0;
	
  //------------------------------------------------------------------------------------------------
	/**
	 * Initialization of global variables, including URL of the Monitoring Service
	 * @param monitoringServiceURL URL of the monitoring service
	 * @param myAppID User-defined Application ID [currently this option is not supported by the basic API] 
	 */
	public MonitoringLib(String monitoringServiceURL, String myAppID)
	{
		String appID = myAppID;
		this.appId = appID; 
		this.monitoringServiceURL = monitoringServiceURL;
		this.monitoringServiceMetricsURL = monitoringServiceURL + "metrics/";
		this.monitoringServiceDetailsURL = monitoringServiceURL + "details/";
		this.monitoringServiceMetricValues = monitoringServiceURL.subSequence(0, monitoringServiceURL.length()-2) + "/stats/";
	}
	//------------------------------------------------------------------------------------------------
	/**
	 * Main method: calling the user-level functions 
	 * 
	 * @param args No input arguments for the main() function are required
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
			
    }
//------------------------------------------------------------------------------------------------
	/**
	 * Retrieving complete list of registered applications
	 * 
	 * @return ArrayList of IDs of all registered applications
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArrayList<String> getApplications() throws ParseException, FileNotFoundException, IOException {
		
		ArrayList<String> appIds = new ArrayList<String>();
		String returnString;
		returnString = getResponse(monitoringServiceURL);
		appIds = parseJsonByTag(appIds, returnString, "id");
		
		return appIds;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * Retrieving complete list of metrics for the given application
	 * 
	 * @param appId Application ID
	 * @return ArrayList of all metrics collected for the given application 
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArrayList<String> getApplicationMetrics(String appId) throws ParseException, FileNotFoundException, IOException {
			
		ArrayList<String> metricsList = new ArrayList<String>();
		String id = appId;
		String returnString;
		returnString = getResponse(monitoringServiceMetricsURL + id);
		metricsList = parseJsonMetrics(metricsList, returnString);
				
		return metricsList;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * Retrieving complete metrics data for the given application
	 * 
	 * @param appId Application ID
	 * @param metric Metric name
	 * @return ArrayList of all metrics collected for the given application 
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArrayList<String> getAppMetricsValues(String appId, String metric) throws ParseException, FileNotFoundException, IOException {
		ArrayList<String> metricsList = new ArrayList<String>();
		String id = appId;
		String returnString;
		returnString = getResponse(monitoringServiceURL + id);
		metricsList = parseJsonByTag(metricsList, returnString, metric);
					
		return metricsList;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * Retrieving data of a given metric with condition of another metric
	 * 
	 * @param appId Application ID
	 * @param metricGet Metric which values are to be obtained
	 * @param metricCond Metric which values are conditions
	 * @param metricCondValue MetricCond value as positive filter for metricGet values
	 * @return ArrayList of all metrics collected for the given application 
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ArrayList<String> getAppMetricsValuesByCondition(String appId, String metricGet,
			String metricCond, String metricCondValue) throws ParseException, FileNotFoundException, IOException {
			
		ArrayList<String> metricGetList = new ArrayList<String>();
		ArrayList<String> metricCondList = new ArrayList<String>();
		ArrayList<String> metricGetListSelected = new ArrayList<String>();
		ArrayList<Double> metricGetListSelectedNum = new ArrayList<Double>();
		String id = appId;
		String returnString = getResponse(monitoringServiceURL + id);
		metricGetList = parseJsonByTag(metricGetList, returnString, metricGet);
		metricCondList = parseJsonByTag(metricCondList, returnString, metricCond);
		iValue = 0; count = 0; min = Double.MAX_VALUE; max = Double.MIN_VALUE; avg = 0; sum = 0;
		System.out.println("metricCond = " + metricCond + "; metricCondValue = " + metricCondValue);
		
		for (int i = 0; i < metricGetList.size(); i++)
		{
			if(!metricGetList.get(i).equals("null"))
			{
				
				if(metricCondList.get(i).equals(metricCondValue)||metricCondList==null||metricCondValue==null)
				{
					metricGetListSelected.add(metricGetList.get(i));
					
					iValue = new Double(metricGetList.get(i));
					count++;
					if(iValue < min) min = iValue;
					if(iValue > max) max = iValue;
					sum = sum + iValue; 
					metricGetListSelectedNum.add(iValue);
				}
			}
			
			avg = sum/count;
		}
						
		return metricGetListSelected;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * Retrieving details of the given application
	 * 
	 * @param appId Application ID
	 * @return Application details as String
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String getApplicationDetails(String appId) throws ParseException, FileNotFoundException, IOException {

		String id = appId;
		String appDetails;
		appDetails = getResponse(monitoringServiceDetailsURL + id);
						
		return appDetails;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * Retrieving metric values
	 * 
	 * @param appId Application ID
	 * @param metricName Name of the metric
	 * @param timeInterval Unix_epoch_time_start/Unix_epoch_time_end in seconds
	 * @return Values of the given metric, represented as String
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String getMetricAggregated(String appId, String metricName, String timeInterval) throws ParseException, FileNotFoundException, IOException {
					
		String id = appId;
		String metricValues;
		metricValues = getResponse(monitoringServiceMetricValues + id + "/" + metricName + "/" + timeInterval);
		//System.out.println("Metric history values = " + metricValues);

		return metricValues;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * REST query to a resource by the given URL
	 * (not a public function)
	 *
	 * @param URL URL of the resource which is requested
	 * @return Response of the REST GET call as String
	 * @throws ParseException
	 */
	private String getResponse(String URL) throws ParseException {
		String responseString = "";
		  
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
		  
			HttpGet httpGet = new HttpGet(URL);

			System.out.println("executing GET request:\n" + httpGet.getRequestLine());

			HttpResponse response;
			response = httpclient.execute(httpGet);
			HttpEntity responseEntity = response.getEntity();

			responseString = EntityUtils.toString(responseEntity);
		  
			httpclient.getConnectionManager().shutdown();
			
		  } catch (ClientProtocolException e) {
			  e.printStackTrace();
		  } catch (IOException e) {
			  e.printStackTrace();
		  }
				
		  return responseString;
	  }
	//------------------------------------------------------------------------------------------------
		/**
		 * @param metricName Name of the metric to be sent
		 * @param metricValue Value of the metric to be sent
		 * @param communicationID communication ID
		 * @param connectionName connection name
		 * @return Response of the REST POST method, represented as String
		 * @throws ParseException
		 */
		public String sendMetricValue(String metricName, String metricValue,
				String communicationID, String connectionName) throws ParseException {
			String responseString = "";
			String resource = monitoringServiceURL + appId;
			long milis = System.currentTimeMillis();
			long seconds = System.currentTimeMillis()/1000;
			String curTime = "" + seconds + "." + (milis-seconds*1000)*10000;
			String requestString = "{\"Timestamp\": "+curTime+", \"hostname\": \"testhost\", \"type\": \"testype\", \""
			+ metricName + "\": " + metricValue + ", \"" + "communicationID" + "\": " + communicationID
			+ ", \"" + "connectionName" + "\": \"" + connectionName + "\" } ";
			  
			System.out.println("requestString = " + requestString);
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(resource);

				System.out.println("executing POST request:\n" + httpPost.getRequestLine());
				
				httpPost.addHeader("content-type", "application/json");
				httpPost.addHeader("Accept","application/json");
				StringEntity params =new StringEntity(requestString);
				httpPost.setEntity(params);
			    
				HttpResponse response;
				response = httpclient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();

				responseString = EntityUtils.toString(responseEntity);
			  
				httpclient.getConnectionManager().shutdown();
				
			  } catch (ClientProtocolException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
					
			  return responseString;
		  }
	//------------------------------------------------------------------------------------------------
		/**
		 * @param appDescription Description of the application to be sent
		 * @param userName User name to be sent
		 * @param hostname Host name of the application to be sent
		 * @return Response of the REST POST method, represented as String
		 * @throws ParseException
		 */
		public String registerApplication(String appDescription, String userName, String hostname) throws ParseException {
			String responseString = "";
			String resource = monitoringServiceURL;
			
			long milis = System.currentTimeMillis();
			long seconds = System.currentTimeMillis()/1000;
			String curTime = "" + seconds + "." + (milis-seconds*1000)*10000;
			String requestString = "{ \"Start_date\": " + curTime + ",  \"Username\": \"" + userName
					+ "\",  \"Hostname\": \""+hostname+"\",  \"Description\": \""+appDescription+"\"  }  ";
			System.out.println("Registering application: " + requestString);
			  
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
		  
				HttpPost httpPost = new HttpPost(resource);

				System.out.println("executing POST request:\n" + httpPost.getRequestLine());
				
				httpPost.addHeader("content-type", "application/json");
				httpPost.addHeader("Accept","application/json");
				StringEntity params =new StringEntity(requestString);
				httpPost.setEntity(params);
			    
				HttpResponse response;
				response = httpclient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();

				if (responseEntity != null) {
					System.out.println("Response content length: " + responseEntity.getContentLength());
				}
			  
				responseString = EntityUtils.toString(responseEntity);
			  
				System.out.println(responseString);
				httpclient.getConnectionManager().shutdown();
				
			  } catch (ClientProtocolException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
			  return responseString;
		  }
//---------------------------------------------------------------------------------------
	private ArrayList<String> parseJsonByTag(ArrayList<String> elementsList, String json, String tag) throws FileNotFoundException, IOException, ParseException
	{
		//PrintWriter writer = new PrintWriter(filepath + "json2txt-" + tag + ".txt", "UTF-8");
		JSONParser parser = new JSONParser();
		Object obj = null;
		try{
			obj = parser.parse(json);
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonArray = (JSONArray) obj;
		for(int i = 0; i <jsonArray.size(); i++)
		{
			jsonObject = (JSONObject) jsonArray.get(i);
			elementsList = readJson(elementsList, jsonObject, null /*writer*/, tag);
		}
			  
        } catch (Exception e) {
            System.out.println("Invalid JSON String data: " + e.toString());
            return elementsList;
        }

		//writer.close();
		return elementsList;
	  }
//---------------------------------------------------------------------------------------
	private ArrayList<String> parseJsonMetrics(ArrayList<String> metricsList, String json) throws FileNotFoundException, IOException, ParseException
	{
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(json);
		JSONArray jsonArray = new JSONArray();
		String metric;
		
		if(obj.getClass()!=jsonArray.getClass())
		{
			System.out.println("Total number of metrics = 0");
			System.out.println("obj = " + obj.toString());
			return metricsList; 
		}
		
		jsonArray = (JSONArray) obj;
		
		System.out.println("Total number of metrics = " + jsonArray.size());
			
		for(int i = 0; i <jsonArray.size(); i++)
		{
			metric = jsonArray.get(i).toString();
			metricsList.add(metric);
			System.out.println("metric # " + i + " = " + metric);
				
		}
		return metricsList;
	}
//---------------------------------------------------------------------------------------
	private ArrayList<String> readJson(ArrayList<String> elementsList, JSONObject jsonObject, PrintWriter writer, String TagName) throws FileNotFoundException, IOException, ParseException {
		String appId = "";
		if (jsonObject.get(TagName) == null) {
			elementsList.add("null");
		} else {
			String Objectscontent = jsonObject.get(TagName).toString();
			if (Objectscontent.startsWith("[{") && Objectscontent.endsWith("}]")) {
				JSONArray jsonArray = (JSONArray) jsonObject.get(TagName);

				for (int temp = 0; temp < jsonArray.size(); temp++) {
					System.out.println("Array:" + jsonArray.toJSONString());
					JSONObject jsonObjectResult = (JSONObject) jsonArray.get(temp);
					System.out.println("Result:" + jsonObjectResult.toJSONString());

					Set<String> jsonObjectResultKeySet = jsonObjectResult.keySet();
					System.out.println("KeySet:" + jsonObjectResultKeySet.toString());

					for (String s : jsonObjectResultKeySet) {
						System.out.println(s);
						readJson(elementsList, jsonObjectResult, writer, s);
					}
				}
			} else {
				appId = jsonObject.get(TagName).toString();
				elementsList.add(appId);
			}
		}
		return elementsList;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * User-specific function; to be implemented
	 * @param appName Application name (not equals Application ID)
	 * @param communicationID communication ID
	 * @param connectionName connection name
	 * @return
	 * @throws ParseException 
	 */
	public Long start(String appName, String commID, String connName) throws ParseException
	{
		Long time = System.currentTimeMillis();
		this.sendMetricValue(appName+"Start", time.toString(), commID, connName);
		return time;
	}
//------------------------------------------------------------------------------------------------
	/**
	 * User-specific function; to be implemented
	 * @param appName Application name (not equals Application ID)
	 * @param communicationID communication ID
	 * @param connectionName connection name
	 * @return
	 * @throws ParseException 
	 */
	public Long stop(String appName, String commID, String connName) throws ParseException
	{
		Long time = System.currentTimeMillis();
		this.sendMetricValue(appName+"End", time.toString(), commID, connName);
		return time;
	}
//---------------------------------------------------------------------------------------
	public int getCountForCondMetric() {
		return count;
	}
	public double getMinForCondMetric() {
		return min;
	}
	public double getMaxForCondMetric() {
		return max;
	}
	public double getAvgForCondMetric() {
		return avg;
	}
	public double getSumForCondMetric() {
		return sum;
	}
	
}
