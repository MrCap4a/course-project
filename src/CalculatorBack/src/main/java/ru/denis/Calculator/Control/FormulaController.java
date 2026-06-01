package ru.denis.Calculator.Control;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.denis.Calculator.Dto.FormulaDto;
import ru.denis.Calculator.Dto.FormulaGroupDto;
import ru.denis.Calculator.Dto.Request.FormulaGroupRequest;
import ru.denis.Calculator.Dto.Request.FormulaRequest;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaGroupService;
import ru.denis.Calculator.Mediator.Interfaces.IFormulaService;

import java.util.List;

@RestController
public class FormulaController {

    private final IFormulaService formulaService;
    private final IFormulaGroupService formulaGroupService;

    public FormulaController(IFormulaService formulaService,
                             IFormulaGroupService formulaGroupService) {
        this.formulaService = formulaService;
        this.formulaGroupService = formulaGroupService;
    }

    // -------------------------------------------------------------------------
    // Formulas
    // -------------------------------------------------------------------------

    @GetMapping("/formulas")
    public ResponseEntity<Page<FormulaDto>> getAllFormulas(
            @RequestParam(required = false) Integer groupId,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(formulaService.getAllFormulas(groupId, pageable));
    }

    @PostMapping("/formulas")
    public ResponseEntity<FormulaDto> createFormula(@RequestBody FormulaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formulaService.createFormula(request));
    }

    @PutMapping("/formulas/{id}")
    public ResponseEntity<FormulaDto> editFormula(@PathVariable Integer id,
                                                  @RequestBody FormulaRequest request) {
        return ResponseEntity.ok(formulaService.editFormula(id, request));
    }

    @DeleteMapping("/formulas/{id}")
    public ResponseEntity<Void> deleteFormula(@PathVariable Integer id) {
        formulaService.deleteFormula(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Formula groups
    // -------------------------------------------------------------------------

    @GetMapping("/formula-groups")
    public ResponseEntity<List<FormulaGroupDto>> getAllFormulaGroups() {
        return ResponseEntity.ok(formulaGroupService.getAllFormulaGroups());
    }

    @PostMapping("/formula-groups")
    public ResponseEntity<FormulaGroupDto> createFormulaGroup(@RequestBody FormulaGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(formulaGroupService.createFormulaGroup(request));
    }

    @PutMapping("/formula-groups/{id}")
    public ResponseEntity<FormulaGroupDto> editFormulaGroup(@PathVariable Integer id,
                                                            @RequestBody FormulaGroupRequest request) {
        return ResponseEntity.ok(formulaGroupService.editFormulaGroup(id, request));
    }

    @DeleteMapping("/formula-groups/{id}")
    public ResponseEntity<Void> deleteFormulaGroup(@PathVariable Integer id) {
        formulaGroupService.deleteFormulaGroup(id);
        return ResponseEntity.noContent().build();
    }
}
