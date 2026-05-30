package ru.denis.Calculator.Dto.Request;

import java.util.List;

public record UserRoleRequest(
        String name,
        List<Integer> permissionIds
) {}
