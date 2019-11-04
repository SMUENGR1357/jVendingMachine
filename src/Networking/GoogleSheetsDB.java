package Networking;

import Structs.Item;
import Structs.User;
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
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GoogleSheetsDB {

    private static final String SPREADSHEET_ID = "1R-coT0qJ24u-lwPZVAzDvq337h2I4wEVH-rsk2u15Ys";
//    private static String USERS = "0";
//    private static final String CREDITS = "1167972744";
//    private static final String ITEMS = "1127496034";

    private static final String APPLICATION_NAME = "KNW2300 Vending Machine Database";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    private static NetHttpTransport HTTP_TRANSPORT;
    private static Sheets service;

    public static void init() throws Exception{
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void loadUsers(ConcurrentHashMap<Long, User> users) throws Exception {
        String range = "VM_Users!A2:D";
        ValueRange response = getResponse(range);
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found on users spreadsheet.");
        } else {
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                users.put(Long.parseLong(row.get(0).toString()), new User(row.get(1).toString(), row.get(3).toString(), Long.parseLong(row.get(0).toString()), row.get(2).toString().equals("1") ? true : false));
            }
        }
    }

    public static void loadCredits(ConcurrentHashMap<String, Integer> teamCredits) throws Exception {
        String range = "VM_Credit!A2:B";
        ValueRange response = getResponse(range);
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found on credit spreadsheet.");
        } else {
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                teamCredits.put(row.get(0).toString(), Integer.parseInt(row.get(1).toString()));
            }
        }

    }

    public static void loadItems(ConcurrentHashMap<Integer, Item> items) throws Exception {
        String range = "VM_Items!A2:D";
        ValueRange response = getResponse(range);
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found on items spreadsheet.");
        } else {
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                items.put(Integer.parseInt(row.get(2).toString()),
                        new Item(row.get(0).toString(), Integer.parseInt(row.get(1).toString()), Integer.parseInt(row.get(2).toString()), Integer.parseInt(row.get(3).toString())));
            }
        }
    }


    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
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

    private static ValueRange getResponse(String range) throws Exception {
        return service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
    }

    public static void writeItems(ConcurrentHashMap<Integer, Item> items) {
        List<List<Object>> toWrite = new ArrayList<>();
        for (Item i : items.values()) {
            toWrite.add(
                    new ArrayList<>(
                            Arrays.asList(i.name, i.cost, i.slot, i.quantity)
                    )
            );
        }
        ValueRange body = new ValueRange()
                .setValues(toWrite);
        try {
            UpdateValuesResponse result = service.spreadsheets().values().update(SPREADSHEET_ID, "VM_Items!A2:D", body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeCredits(ConcurrentHashMap<String, Integer> teamCredit) {
        List<List<Object>> toWrite = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : teamCredit.entrySet()) {
            toWrite.add(
                    new ArrayList<>(
                            Arrays.asList(entry.getKey(), entry.getValue())
                    )
            );
        }
        ValueRange body = new ValueRange()
                .setValues(toWrite);
        try {
            UpdateValuesResponse result = service.spreadsheets().values().update(SPREADSHEET_ID, "VM_Credit!A2:B", body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeUsers(ConcurrentHashMap<Long, User> users) {
        List<List<Object>> toWrite = new ArrayList<>();
        for (User u : users.values()) {
            toWrite.add(
                    new ArrayList<>(
                            Arrays.asList(u.id, u.name, u.admin, u.team)
                    )
            );
        }
        ValueRange body = new ValueRange()
                .setValues(toWrite);
        try {
            UpdateValuesResponse result = service.spreadsheets().values().update(SPREADSHEET_ID, "VM_Users!A2:D", body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//
//    /**
//     * Global instance of the scopes required by this quickstart.
//     * If modifying these scopes, delete your previously saved tokens/ folder.
//     */
//    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
//    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
//
//    /**
//     * Creates an authorized Credential object.
//     * @param HTTP_TRANSPORT The network HTTP Transport.
//     * @return An authorized Credential object.
//     * @throws IOException If the credentials.json file cannot be found.
//     */
//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        // Load client secrets.
//        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }
//
//    /**
//     * Prints the names and majors of students in a sample spreadsheet:
//     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
//     */
//    public static void main(String... args) throws IOException, GeneralSecurityException {
//        // Build a new authorized API client service.
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//        Spreadsheet spreadsheet = new Spreadsheet()
//                .setProperties(new SpreadsheetProperties()
//                        .setTitle("VM_Users"));
//        spreadsheet = service.spreadsheets().create(spreadsheet)
//                .setFields("spreadsheetId")
//                .execute();
//        System.out.println("Spreadsheet ID: " + spreadsheet.getSpreadsheetId());
//
//System.exit(0);
//
//
//        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
//        final String range = "Class Data!A2:E";
//
//        ValueRange response = service.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found.");
//        } else {
//            System.out.println("Name, Major");
//            for (List row : values) {
//                // Print columns A and E, which correspond to indices 0 and 4.
//                System.out.printf("%s, %s\n", row.get(0), row.get(4));
//            }
//        }
//    }
}