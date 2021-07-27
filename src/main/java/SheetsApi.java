import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SheetsApi {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String spreadsheetId = "SPREADSHEET-ID-HERE";
    private static final String range = "DESIRED-RANGE-HERE";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetsApi.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static ValueRange sendGetRequest(String range) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        //send the request
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response;
    }

    public static UpdateValuesResponse sendUpdateRequest(String range, ValueRange content) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        //send the request
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        UpdateValuesResponse response = service.spreadsheets().values()
                .update(spreadsheetId, range, content)
                .setValueInputOption("USER_ENTERED")
                .execute();
        return response;
    }

    public static String colDateToCell(String col, LocalDateTime date){
        String row = String.valueOf(Bot.SEP_FIRST.until(date, ChronoUnit.DAYS) + Bot.SEP_FIRST_OFFSET);
        return col+row;
    }

    public static Boolean UpdateSingleValue(String cell, String value){
        try{
            List<List<Object>> lst= Arrays.asList(Arrays.asList(value));
            ValueRange content = new ValueRange().setValues(lst);
            sendUpdateRequest("Sheet1!" + cell + ":" + cell, content);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public static String getSingleValue(String cell) throws IOException, GeneralSecurityException {
        List<List<Object>> vals = sendGetRequest("Sheet1!" + cell + ":" + cell).getValues();
        if (vals == null || vals.size()==0){
            return null;
        }
        return (String) vals.get(0).get(0);
    }

    public static String getDateRating(String col, LocalDateTime date) throws GeneralSecurityException, IOException {
        return getSingleValue(colDateToCell(col, date));

    }

    public static Boolean isCellFilled(String col, LocalDateTime date) throws GeneralSecurityException, IOException {
        return getDateRating(col, date) != null;
    }

    public static List<String> getDailyRatings(String col) throws IOException, GeneralSecurityException {
        final String range = "Sheet1!" + col + "8:" + col;
        List<List<Object>> values = sendGetRequest(range).getValues();
        values.forEach(x -> x.forEach(System.out::println));
        return values.stream().map(x -> (String) x.get(0)).collect(Collectors.toList());
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        values.forEach(x -> x.forEach(System.out::println));
        getDailyRatings("L");
        System.out.println("---------");
        System.out.println(getDateRating("L", Utils.getDateFromDM(2, 9)));
        System.out.println();

    }
}