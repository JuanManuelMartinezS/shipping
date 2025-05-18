package poo;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;
import io.javalin.http.Context;
import poo.helpers.Controller;
import poo.helpers.Utils;
import poo.model.Box;
import poo.model.Client;
import poo.model.Delivery;
import poo.model.DeliveryStatus;
import poo.model.Envelope;
import poo.model.Merchandise;
import poo.model.Pack;
import poo.model.Sack;
import poo.services.BoxService;
import poo.services.ClientService;
import poo.services.DeliveryService;
import poo.services.EnvelopeService;
import poo.services.MerchandiseService;
import poo.services.Service;

public final class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class); // Funciona como un sout

    public static void main(String[] args) throws Exception {
        int port = 7070;
        // https://javalin.io/tutorials/javalin-logging#using-any-of-those-loggers
        String message = String.format(
                "%sIniciando la API Rest de Envios y bodegaje. Use Ctrl+C para detener la ejecución%s", Utils.CYAN,
                Utils.RESET);
        LOG.info(message);

        Utils.trace = true; // no deshabilite la traza de errores hasta terminar completamente la aplicación
        int length = args.length;
        if (length > 0) {
            Utils.trace = Boolean.parseBoolean(args[0]);
            if (length >= 2) {
                port = Integer.parseInt(args[1]);
            }
        }

        if (Utils.trace) {
            // ver para tiempo de desarrollo: ./.vscode/launch.json
            LOG.info(String.format("%sHabilitada la traza de errores%s", Utils.YELLOW, Utils.RESET));
        } else {
            LOG.info(String.format("%sEnvíe un argumento true|false para habilitar|deshabilitar la traza de errores%s",
                    Utils.YELLOW, Utils.RESET));
        }

        // esencial para estandarizar el formato monetario con separador de punto
        // decimal, no con coma
        Locale.setDefault(Locale.of("es_CO"));

        Service<Client> clientService = new ClientService();
        Service<Merchandise> merchandiseService = new MerchandiseService(clientService);
        Service<Delivery> packService = new DeliveryService(Pack.class, clientService);
        Service<Delivery> envelopeService = new EnvelopeService(Envelope.class, clientService);
        // // *-*-*- OJO - *-*-*
        Service<Delivery> boxService = new BoxService(Box.class, clientService);
        Service<Delivery> sackService = new DeliveryService(Sack.class, clientService);
        // ...........................................

        Javalin
                .create(config -> {
                    config.http.defaultContentType = "application/json";
                    // ver https://javalin.io/plugins/cors#getting-started
                    config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));

                    config.router.apiBuilder(() -> {
                        new Controller<>(clientService).info();
                        new Controller<>(merchandiseService).info();
                        new Controller<>(packService).info();
                        new Controller<>(envelopeService).info();
                        new Controller<>(boxService).info();
                        new Controller<>(sackService).info();
                    });
                })
                .start(port)
                .get("/", ctx -> ctx.json("{ \"data\": \"Welcome to the logistics service\", \"message\": \"ok\" }"))
                .get("/delivery/statuses", ctx -> ctx.json(DeliveryStatus.getAll().toString()))
                .afterMatched(ctx -> updateClients(ctx))
                .exception(
                        Exception.class,
                        (e, ctx) -> {
                            Utils.printStackTrace(e);
                            String error = Utils.keyValueToStrJson("message", e.getMessage(), "request", ctx.fullUrl());
                            ctx.json(error).status(400);
                        });

        Runtime
                .getRuntime()
                .addShutdownHook(
                        new Thread(() -> {
                            LOG.info(String.format("%sEl servidor Jetty de Javalin ha sido detenido%s%n", Utils.RED,
                                    Utils.RESET));
                        }));
    }

    /**
     * Si ocurrió una petición PATCH para el servicio de clientes
     * En una variable cliente de tipo JSONObject asignar el cliente actualizado
     * Para cada archivo: Mercancia, Paquete, Sobre, Caja o Bulto
     * actualizar las posibles ocurrencias de cliente
     * recargar la instancia de servicio afectada con la actualización
     * 
     * @throws Exception
     */
    // Método para actualizar archivos JSON en la carpeta "data"
    private static void updateClients(@NotNull Context ctx) throws Exception {
        // Si se actualiza un cliente con PATCH
        if (ctx.path().contains("client") && ctx.method().toString().equals("PATCH")) {
            // Asignar en una variable JSONObject el cliente actualizado
            JSONObject client = new JSONObject(ctx.result()).getJSONObject("data");

            // Para cada archivo: Mercancia, Paquete, Sobre, Caja o Bulto
            // actualizar las posibles ocurrencias de cliente
            Utils.ifExistsUpdateFile(client, "storer", "Merchandise");
            Utils.ifExistsUpdateFile(client, "sender", "Pack");
            Utils.ifExistsUpdateFile(client, "addressee", "Pack");
            Utils.ifExistsUpdateFile(client, "sender", "Envelope");
            Utils.ifExistsUpdateFile(client, "addressee", "Envelope");
            Utils.ifExistsUpdateFile(client, "sender", "Box");
            Utils.ifExistsUpdateFile(client, "addressee", "Box");
            Utils.ifExistsUpdateFile(client, "sender", "Sack");
            Utils.ifExistsUpdateFile(client, "addressee", "Sack");

        }

    }
}