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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TestMonLib {
//------------------------------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
	
	//initialization: including Application_ID and URL
	//String monitoringServiceURL = "http://XYZ:3000/executions";
	String monitoringServiceURL = "http://mf.excess-project.eu/executions/";
	String myAppID = "SendReceiveAppID2";	
	MonitoringLib monitor = new MonitoringLib(monitoringServiceURL, myAppID);
	
	//sending the pre-execution metrics and their values into the DB
	//(implicit within the start() function)
	Long send_receive_calls = (long) 0;
	Long send_receive_start_time = monitor.start("SendReceive", "0", "connection");
	
	//executing the experimental function  
	sendReceive();
	send_receive_calls++;
    
	//sending the post-execution metrics and their values into the DB
	//(implicit within the stop() function)
	Long send_receive_end_time = monitor.stop("SendReceive", "0", "connection");
	monitor.sendMetricValue("SendReceiveCalls", send_receive_calls.toString(), "0", "connection");
    
	Long duration = send_receive_end_time - send_receive_start_time;
	Long averageDuration = duration / send_receive_calls;
	monitor.sendMetricValue("SendReceiveAverageDuration", averageDuration.toString(), "0", "connection");
	
	//------------------------------------------------------------------------------------
	System.out.println("Sleeping for 3 seconds before retrieving values...");
	TimeUnit.SECONDS.sleep(3);
	
	String appId = myAppID;
	ArrayList<String> metrics_list = monitor.getApplicationMetrics(appId);
	String stringMetricsResponse;
	String timeInterval = "0/1424785785395";
	for (int i = 0; i < metrics_list.size(); i++)
	{
		stringMetricsResponse = monitor.getMetricAggregated(appId, metrics_list.get(i), timeInterval);
		System.out.println();
		if(stringMetricsResponse.equals("No data in the DB"))
			System.out.println("getMetricAggregated(): '" + metrics_list.get(i) + "': Aggregated values are not available");
		else
			System.out.println("getMetricAggregated(): '" + metrics_list.get(i) + "' values : " + stringMetricsResponse);
		
		ArrayList<String> metrics_list2 = monitor.getAppMetricsValues(appId, metrics_list.get(i));
		System.out.println("getAppMetricsValues(): " +metrics_list.get(i)+ " = " + metrics_list2.toString());
		
	}
	String metricByCondition = "SendReceiveAverageDuration";
	String conditionMetric = "communicationID"; //"connectionName"
	String conditionValue = "0"; //"testconnection"
	ArrayList<String> metricsByCond = monitor.getAppMetricsValuesByCondition(
			appId, metricByCondition, conditionMetric, conditionValue);
	System.out.println();
	System.out.println("getAppMetricsValuesByCondition(): " + metricByCondition + " = " + metricsByCond.toString());
	if(monitor.getCountForCondMetric()>0)
		System.out.println("count = " + monitor.getCountForCondMetric() + "; min = " + monitor.getMinForCondMetric()+ "; max = " + monitor.getMaxForCondMetric() +
			"; avg = " + monitor.getAvgForCondMetric() + "; sum = " + monitor.getSumForCondMetric());
	else
		System.out.println("No values were found for the given metric and condition");
/*
	System.out.println();
	ArrayList<String> metrics_listIO = monitor.getAppMetricsValuesByCondition(
			appId, "iostat:xvda:tps", null, null);
	System.out.println("getAppMetricsValuesByCondition(): " + "iostat:xvda:tps" + " = " + metrics_listIO.toString());
	if(monitor.getCountForCondMetric()>0)
		System.out.println("count = " + monitor.getCountForCondMetric() + "; min = " + monitor.getMinForCondMetric()+ "; max = " + monitor.getMaxForCondMetric() +
			"; avg = " + monitor.getAvgForCondMetric() + "; sum = " + monitor.getSumForCondMetric());
	else
		System.out.println("No values were found for the given metric and condition");
	
 */
	}	
//------------------------------------------------------------------------------------------------
	public static void sendReceive() throws InterruptedException{
		 System.out.println("Sleeping for 1 second to simulate the experimental function...");
		 TimeUnit.SECONDS.sleep(1);
		
	}
//------------------------------------------------------------------------------------------------
}
