package ru.denis.Calculator.Control;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.denis.Calculator.Dto.Request.SqlQueryRequest;
import ru.denis.Calculator.Dto.SqlResultDto;
import ru.denis.Calculator.Dto.SqlSchemaDto;
import ru.denis.Calculator.Mediator.PermissionChecker;
import ru.denis.Calculator.Mediator.Impl.SqlServiceImpl;

@RestController
@RequestMapping("/sql")
public class SqlController {

    private final SqlServiceImpl sqlService;
    private final PermissionChecker checker;

    public SqlController(SqlServiceImpl sqlService, PermissionChecker checker) {
        this.sqlService = sqlService;
        this.checker = checker;
    }

    @PostMapping("/execute")
    public ResponseEntity<SqlResultDto> execute(@RequestBody SqlQueryRequest request) {
        checker.require("sql.execute");
        SqlResultDto result = sqlService.executeQuery(request.query());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schema")
    public ResponseEntity<SqlSchemaDto> schema() {
        checker.require("sql.execute");
        return ResponseEntity.ok(sqlService.getSchema());
    }
}
