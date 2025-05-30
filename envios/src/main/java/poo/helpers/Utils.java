package poo.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // <-------- OJO
import java.util.List;
import java.util.Properties;
import java.util.Random; // <-------- OJO

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.Property;

public class Utils {

  public static final String RESET = "\u001B[0m";
  public static final String BLACK = "\u001B[30m";
  public static final String RED = "\u001B[31m";
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE = "\u001B[34m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN = "\u001B[36m";
  public static final String WHITE = "\u001B[37m";

  public static final String PATH = "./data/";
  public static boolean trace = false;

  private Utils() {
  } // lo mismo en Keyboard

  public static void printStackTrace(Exception e) {
    if (Utils.trace) {
      System.out.printf("%s%s%s%s%s%n", Utils.RED, "-".repeat(30), " Reporte de excepciones ", "-".repeat(30),
          Utils.RESET);
      e.printStackTrace(System.out);
      System.out.printf("%s%s%s%s%s%n", Utils.RED, "-".repeat(30), " Fin del reporte de excepciones ", "-".repeat(30),
          Utils.RESET);
    }
  }

  // Genera un String Aleatorio
  public static String getRandomKey(int stringLength) {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 90; // letter 'Z'
    Random random = new Random();

    String generatedString = random
        .ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(stringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();

    return generatedString;
  }

  public static String strDateTime(LocalDateTime dateTime) {
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    return dateTime.format(formato);
  }

  public static boolean fileExists(String fileName) {
    // Utiliza la clase Paths de java
    Path dirPath = Paths.get(fileName);
    return Files.exists(dirPath) && !Files.isDirectory(dirPath);
  }

  public static boolean pathExists(String path) {
    Path folder = Paths.get(path);
    return Files.exists(folder) && Files.isDirectory(folder);
  }

  public static void createFolderIfNotExist(String folder) throws IOException {
    // si no existe o si existe y no es una carpeta, crear la carpeta
    if (!pathExists(folder)) {
      Path dirPath = Paths.get(folder);
      Files.createDirectories(dirPath);
    }
  }

  public static String getPath(String path) {
    Path parentPath = Paths.get(path).getParent();
    return parentPath == null ? null : parentPath.toString();
  }

  /**
   * Crea la ruta padre indicada en el argumento recibido si no existe
   * 
   * @param filePath Un String que representa una ruta válida
   * @return Una instancia de Path con la ruta original
   * @throws IOException
   */
  public static Path initPath(String filePath) throws IOException {
    String path = getPath(filePath);
    createFolderIfNotExist(path);
    return Paths.get(filePath);
  }

  public static String readText(String fileName) throws IOException {
    Path path = Paths.get(fileName);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  public static void _writeText(List<?> list, String fileName) throws Exception {
    initPath(fileName);
    try (FileWriter fw = new FileWriter(new File(fileName), StandardCharsets.UTF_8);
        BufferedWriter writer = new BufferedWriter(fw)) {
      for (int i = 0; i < list.size(); i++) {
        writer.append(list.get(i).toString());
        writer.newLine();
      }
    }
  }

  public static void writeText(List<?> list, String fileName) throws Exception {
    // https://www.tabnine.com/code/java/methods/java.nio.file.Files/newBufferedWriter
    Path path = initPath(fileName);
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (Object o : list) {
        writer.append(o.toString());
        writer.newLine();
      }
    }
  }

  public static void writeText(String content, String fileName) throws IOException {
    Path path = initPath(fileName);
    // Files es otra clase de Java
    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
  }

  public static void writeJSON(List<?> list, String fileName) throws IOException {
    JSONArray jsonArray = new JSONArray(list);
    writeText(jsonArray.toString(2), fileName);
  }

  /**
   * Convierte parámetros de una URL en una representación JSON
   * 
   * @param s Algo así como param1=value1&param2=value2...
   * @return Un String JSON con los pares paramX=valueX de s
   * @throws IOException
   */
  public static String paramsToJson(String s) throws IOException {
    s = s.replace("&", "\n");
    StringReader reader = new StringReader(s);
    Properties properties = new Properties();
    properties.load(reader);
    return Property.toJSONObject(properties).toString(2);
  }

  /**
   * Convierte un número par de strings en una representación json {key:value,
   * ...}
   * 
   * @param strings los strings (en número par) que se convierten a json
   * @return Un String JSON con los pares key=value de strings
   */
  public static JSONObject keyValueToJson(String... strings) {
    JSONObject json = new JSONObject();
    for (int i = 0; i < strings.length; i += 2) {
      json.put(strings[i], strings[i + 1]);
    }
    return json;
  }

  public static String keyValueToStrJson(String... strings) {
    return keyValueToJson(strings).toString();
  }

  /**
   * Verifica en cualquier archivo de tipo JSON si un objeto con una propiedad
   * determinada, está
   * contenido en uno de los objetos JSON que conforman el array de objetos JSON
   * contenido en el archivo.
   * 
   * @param fileName El nombre del archivo sin extensión, que contiene el array de
   *                 objetos JSON
   * @param key      La clave o atributo que identifica el objeto JSON a buscar
   *                 dentro de cada objeto
   * @param search   El objeto JSON a buscar
   * @param property La clave del objeto que se usa para hacer la comparación.
   *                 Ej.: "id"
   * @return True si se encuentra que search alguno de los objetos del array
   * @throws Exception
   */
  public static boolean exists(String fileName, String key, JSONObject search, String property) throws Exception {
    if (!Utils.fileExists(fileName + ".json")) {
      return false;
    }
    String data = readText(fileName + ".json");
    JSONArray jsonArrayData = new JSONArray(data);

    for (int i = 0; i < jsonArrayData.length(); i++) {
      // obtener el JSONObject del array en la iteración actual
      JSONObject jsonObj = jsonArrayData.getJSONObject(i);

      if (jsonObj.has(key)) {
        // De la instancia actual obtener el objeto JSON que se requiere verificar
        jsonObj = jsonObj.getJSONObject(key);
        // OJO >>> utilizar una de las propiedades de los objetos para hacer la
        // comparación
        if (jsonObj.optString(property).equals(search.optString(property))) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Verifica en cualquier archivo de tipo JSON si un objeto está contenido en uno
   * de los objetos
   * JSON que conforman el array de objetos JSON contenido en el archivo.
   * 
   * @param fileName El nombre del archivo sin extensión, que contiene el array de
   *                 objetos JSON
   * @param key      La clave o atributo que identifica el objeto JSON a buscar
   *                 dentro de cada objeto
   * @param search   El objeto JSON a buscar
   * @return True si se encuentra que search alguno de los objetos del array
   * @throws Exception
   */
  public static boolean exists(String fileName, String key, JSONObject search) throws Exception {
    if (!Utils.fileExists(fileName + ".json")) {
      return false;
    }

    String data = readText(fileName + ".json");
    JSONArray jsonArrayData = new JSONArray(data);

    for (int i = 0; i < jsonArrayData.length(); i++) {
      // obtener el JSONObject del array en la iteración actual
      JSONObject jsonObj = jsonArrayData.getJSONObject(i);

      if (jsonObj.has(key)) {
        // De la instancia actual obtener el objeto JSON que se requiere verificar
        jsonObj = jsonObj.getJSONObject(key);
        // OJO >>> comparar los strings de ambos JSONObject
        if (jsonObj.toString().equals(search.toString())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Metodo implementado en la app (updateClients) para actualizar las ocurrencias
   * de cliente( destinatario y remitente) y recargar la instancia de servicio con
   * su actualización
   */
  public static void ifExistsUpdateFile(JSONObject client, String typeClient, String fileName) throws Exception {
    boolean updated = false;
    String filePath = Utils.PATH + fileName + ".json";
    // Verificar que el archivo existe
    if (!Files.exists(Paths.get(filePath))) {
      System.out.println("El archivo no existe: " + filePath);
      return;
    }
    // Leer el contenido del archivo JSON
    String content = readText(filePath);
    JSONArray jsonArray = new JSONArray(content);

    // Recorre el json array
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonObj = jsonArray.getJSONObject(i);

      // Si el jsonObj en la posición i tiene el typeClient
      if (jsonObj.has(typeClient)) {
        JSONObject obj = jsonObj.getJSONObject(typeClient);

        // Verifica si es el cliente a actualizar
        if (obj.getString("id").equals(client.getString("id"))) {

          // Actualiza solo las propiedades específicas de `client` en `obj`
          for (String key : client.keySet()) {
            obj.put(key, client.get(key));
          }

          updated = true;
        }
      }
    }
    if (updated) {
      writeJSON(jsonArray.toList(), Utils.PATH + fileName + ".json");
      System.out.println("actualicé en " + fileName + ".json");
    }
  }

  /**
   * Método de clase stringOk con visibilidad pública que retorna un String.
   * Recibe tres argumentos: el primero una cadena que corresponde a una key de un
   * JSONObject, el segundo, la longitud de caracteres mínima que debe tener el
   * value de tipo string asociado a la key y el tercero, el JSONObject en donde
   * está el par key:value.
   * El método stringOk lanza una IllegalArgumentException con el siguiente
   * mensaje cuando el value asociado a la key no tiene al menos la longitud
   * especificada como segundo argumento:
   * Se esperaban al menos ### caracteres: key='value'
   * En caso contrario retorno el valor de la clave asociada al primer argumento.
   */
  public static String stringOk(String key, int min, JSONObject value) {
    if (!value.has(key)) {
      throw new IllegalArgumentException(String.format("The %s key does not exist", key));
    }
    if (value.getString(key).length() < min) {
      throw new IllegalArgumentException(
          String.format("At least %d characters were expected: %s='%s'.", min, key, value.getString(key)));
    } else {
      return value.getString(key);
    }
  }

  /*
   * Método de clase doubleOk con visibilidad pública que retorna un double.
   * Recibe cuatro argumentos: el primero una cadena que corresponde a una key de
   * un JSONObject, el segundo y el tercero, dos enteros que representan el valor
   * mínimo y máximo que puede tener el value de tipo double asociado a la key y
   * el cuarto, el JSONObject en donde está el par key:value.
   * El método doubleOk lanza una IllegalArgumentException con el siguiente
   * mensaje cuando el value asociado a la key no está en el rango especificado
   * por los parámetros min y max:
   * Se esperaba un valor entre ##.# y ##.#: key=value
   * En caso contrario retorno el valor de la clave asociada al primer argumento.
   */
  public static double doubleOk(String key, double min, double max, JSONObject value) {
    double valor = value.getDouble(key);
    if (!value.has(key)) {
      throw new IllegalArgumentException(String.format("The %s key does not exist", key));
    }
    if (valor < min || valor > max) {
      throw new IllegalArgumentException(
          String.format("A value between %.1f and %.1f was expected: %s='%.1f'", min, max, key, value.getDouble(key)));
    }
    return valor;
  }

  public static String MD5(String s) throws Exception {
    MessageDigest m = MessageDigest.getInstance("MD5");
    m.update(s.getBytes(), 0, s.length());
    return new BigInteger(1, m.digest()).toString(16);
  }
}
