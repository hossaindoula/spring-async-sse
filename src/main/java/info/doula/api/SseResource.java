package info.doula.api;

import info.doula.bean.Message;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/")
public class SseResource {
    private static final Logger log = Logger.getLogger(SseResource.class);
    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(path = "/stream")
    public SseEmitter stream() throws IOException {

        SseEmitter sseEmitter = new SseEmitter();

        emitters.add(sseEmitter);
        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));

        return sseEmitter;
    }

    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void chat(@RequestBody Message message) {
        log.info("Got message" + message);

        emitters.forEach((SseEmitter emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("message").data(message));
            } catch (IOException e) {
                log.error("The error was occurred at : ", e);
            }
        });

    }
}
