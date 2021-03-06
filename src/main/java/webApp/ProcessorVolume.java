package webApp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.exceptions.UnirestException;


/**
 * Calculates the sum of bitcoin trading volume
 * from several markets grouped by currency.
 * @author Kalle Paradis
 */
public final class ProcessorVolume {

	//Prevent instantiation of this class since all methods are static.
	private ProcessorVolume() {}
	
	/**
	 * Read input data from file. This method is meant for testing.
	 */
	private static String readDataFromFile(String fileName) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		StringBuilder sb = new StringBuilder();
		while (true) {
			String word = r.readLine();
			if (word == null) {
				break;
			}
			sb.append(word);
		}
		return sb.toString();
	}

	/**
	 * * Calculates and returns the sum of bitcoin volume traded on several markets grouped by currency.
	 * @param liveData If true, get data from external API. Else read from file(for testing).
	 * @return A JSONObject containing the result.
	 */
	public static JSONObject getBTCVolumeByCurrency(boolean liveData) throws JSONException, IOException, UnirestException {
		JSONArray markets = null;
		//Fetch data from API or text-file
		if(liveData) {
			markets = DataFetcher.fetchAllBTCMarkets();
		} else {
			markets = new JSONArray(readDataFromFile("files/MarketData.txt"));
		}
		
		//Read and extract relevant market data.
		Map<String, Double> volumeByCurrencyMap = new TreeMap<>();
		for (int i = 0; i < markets.length(); i++) {
			JSONObject market = markets.getJSONObject(i);
			String currency = market.getString("currency");
			double marketVolume = market.getDouble("volume");
			if (marketVolume > 0) {
				if(volumeByCurrencyMap.containsKey(currency)) {
					double currentVolume = volumeByCurrencyMap.get(currency);
					volumeByCurrencyMap.put(currency, new Double(currentVolume + marketVolume));
				} else {
					volumeByCurrencyMap.put(currency, new Double(marketVolume));
				}
			}
		}
		
		//Build JSONObject
		JSONObject jsonVolumeByCurrency = new JSONObject();
		for(String currency : volumeByCurrencyMap.keySet()) {
			double volume = volumeByCurrencyMap.get(currency);
			jsonVolumeByCurrency.put(currency, volume);
		}
		return jsonVolumeByCurrency;
	}
	
	public static void main(String[] args) throws Exception {
		//For testing, prints an example of the output data.
		System.out.println(ProcessorVolume.getBTCVolumeByCurrency(false).toString(2));
	}
}
