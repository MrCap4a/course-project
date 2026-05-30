package ru.denis.Calculator.Mediator;

public enum DeleteGroupStrategy {
    CASCADE,  // удалить группу вместе со всеми материалами
    DEFAULT,  // перенести материалы в группу "Без группы"
    MOVE      // перенести материалы в указанную группу (targetGroupId)
}
