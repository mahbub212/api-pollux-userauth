package id.co.msiglife.pollux.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.msiglife.pollux.dto.ApiResponse;
import id.co.msiglife.pollux.kafkalogger.KafkaMessageProducer;
import id.co.msiglife.pollux.util.JsonUtil;
import org.example.LogMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    RequestContext requestContext;

    @Autowired
    private JsonUtil jsonUtil;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Input tidak valid");
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(errorMessage);
        response.setData(errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<String>> handleSQLException(SQLException ex) {
        String jsonPayloadString = requestContext.getPayload();
        Map<String, Object> logAndResponse = generateLogAndApiResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY.value(), "Terjadi kesalahan pada database.", ex.getMessage(), jsonPayloadString, 1);
        String msg = (String) logAndResponse.get("log");
        ApiResponse<String> response = (ApiResponse<String>) logAndResponse.get("response");

        kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String jsonPayloadString = requestContext.getPayload();
        Map<String, Object> logAndResponse = generateLogAndApiResponse(ex, HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null, jsonPayloadString, 1);
        String msg = (String) logAndResponse.get("log");
        ApiResponse<?> response = (ApiResponse<?>) logAndResponse.get("response");

        kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolation(ConstraintViolationException ex) {
        String jsonPayloadString = requestContext.getPayload();
        Map<String, Object> logAndResponse = generateLogAndApiResponse(ex, HttpStatus.BAD_REQUEST.value(), "Input tidak valid", ex.getMessage(), jsonPayloadString, 1);
        String msg = (String) logAndResponse.get("log");
        ApiResponse<String> response = (ApiResponse<String>) logAndResponse.get("response");

        kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFoundException(NotFoundException ex) {
        String jsonPayloadString = requestContext.getPayload();
        Map<String, Object> logAndResponse = generateLogAndApiResponse(ex, HttpStatus.NOT_FOUND.value(), ex.getMessage(), null, jsonPayloadString, ex.getProcessId());
        String msg = (String) logAndResponse.get("log");
        ApiResponse<String> response = (ApiResponse<String>) logAndResponse.get("response");

        kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InactiveException.class)
    public ResponseEntity<ApiResponse<String>> handleInactiveException(InactiveException ex) {
        String jsonPayloadString = requestContext.getPayload();
        Map<String, Object> logAndResponse = generateLogAndApiResponse(ex, HttpStatus.FORBIDDEN.value(), ex.getMessage(), null, jsonPayloadString, ex.getProcessId());
        String msg = (String) logAndResponse.get("log");
        ApiResponse<String> response = (ApiResponse<String>) logAndResponse.get("response");

        kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ApiResponse<String>> handleIncorrectPasswordException(IncorrectPasswordException ex) {
        String jsonPayloadString = requestContext.getPayload();
        Map<String, Object> logAndResponse = generateLogAndApiResponse(ex, HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null, jsonPayloadString, ex.getProcessId());
        String msg = (String) logAndResponse.get("log");
        ApiResponse<String> response = (ApiResponse<String>) logAndResponse.get("response");

        kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public static String generateResponseJson(ApiResponse<String> response){
        String json;
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(response);
        } catch (Exception e) {
            json = "{\"error\":\"Failed to serialize response\"}";
        }
        return json;
    }

    public static String generateExceptionJson(Exception ex) {
        try {
            StackTraceElement element = ex.getStackTrace()[0];

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("exception", ex.toString());
            map.put("file", element.getFileName());
            map.put("class", element.getClassName());
            map.put("method", element.getMethodName());
            map.put("line", element.getLineNumber());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

        } catch (Exception e) {
            return "{\"error\": \"Failed to serialize exception\"}";
        }
    }

    public static Map<String, Object> generateLogAndApiResponse(Exception ex, int code, String message, String data, String strPayload, int processId) {
        Map<String, Object> logAndResponse = new LinkedHashMap<>();

        ApiResponse<String> response = new ApiResponse<>(code, message, data);
        logAndResponse.put("response", new ApiResponse<>(code, message, data));

        String errorMsg = generateExceptionJson(ex);
        String responseJson = generateResponseJson(response);
        logAndResponse.put("log", new LogMessage(111, new Date(), new Date(), 111, processId, responseJson, strPayload, "1", errorMsg, true).getMessage());

        return logAndResponse;
    }
}
