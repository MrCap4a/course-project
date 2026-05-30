package ru.denis.Calculator.Control;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.denis.Calculator.Dto.MaterialDto;
import ru.denis.Calculator.Dto.MaterialGroupDto;
import ru.denis.Calculator.Dto.Request.MaterialGroupRequest;
import ru.denis.Calculator.Dto.Request.MaterialRequest;
import ru.denis.Calculator.Mediator.DeleteGroupStrategy;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialGroupService;
import ru.denis.Calculator.Mediator.Interfaces.IMaterialService;

import java.util.List;

@RestController
public class MaterialController {

    private final IMaterialService materialService;
    private final IMaterialGroupService materialGroupService;

    public MaterialController(IMaterialService materialService,
                              IMaterialGroupService materialGroupService) {
        this.materialService = materialService;
        this.materialGroupService = materialGroupService;
    }

    // -------------------------------------------------------------------------
    // Materials
    // -------------------------------------------------------------------------

    @GetMapping("/materials")
    public ResponseEntity<List<MaterialDto>> getAllMaterials(
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(materialService.getAllMaterials(groupId, search));
    }

    @PostMapping("/materials")
    public ResponseEntity<MaterialDto> createMaterial(@RequestBody MaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.createMaterial(request));
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<MaterialDto> editMaterial(@PathVariable Integer id,
                                                    @RequestBody MaterialRequest request) {
        return ResponseEntity.ok(materialService.editMaterial(id, request));
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Integer id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Material groups
    // -------------------------------------------------------------------------

    @GetMapping("/material-groups")
    public ResponseEntity<List<MaterialGroupDto>> getAllMaterialGroups() {
        return ResponseEntity.ok(materialGroupService.getAllMaterialGroups());
    }

    @PostMapping("/material-groups")
    public ResponseEntity<MaterialGroupDto> createMaterialGroup(@RequestBody MaterialGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materialGroupService.createMaterialGroup(request));
    }

    @PutMapping("/material-groups/{id}")
    public ResponseEntity<MaterialGroupDto> editMaterialGroup(@PathVariable Integer id,
                                                              @RequestBody MaterialGroupRequest request) {
        return ResponseEntity.ok(materialGroupService.editMaterialGroup(id, request));
    }

    @DeleteMapping("/material-groups/{id}")
    public ResponseEntity<Void> deleteMaterialGroup(
            @PathVariable Integer id,
            @RequestParam DeleteGroupStrategy strategy,
            @RequestParam(required = false) Integer targetGroupId) {
        materialGroupService.deleteMaterialGroup(id, strategy, targetGroupId);
        return ResponseEntity.noContent().build();
    }
}
