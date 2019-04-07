package ru.chernyshev.control.utils;

import org.springframework.util.CollectionUtils;
import ru.chernyshev.control.dto.FlyProgram;
import ru.chernyshev.control.dto.Operation;
import ru.chernyshev.control.type.ConfigurationParam;
import ru.chernyshev.control.type.ProgramErrorType;

import java.util.*;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

/**
 * Проверка операций из программы полета
 */
public class OperationValidator {

    /**
     * @return true если все параметры задачи валидны
     */
    public static boolean isValid(Operation operation) {
        return operation.getTimeout() > 0 &&
                operation.getId() > 0 &&
                ConfigurationParam.isValid(operation.getVariable(), operation.getValue());
    }

    /**
     * Валидация программы полета
     *
     * @return карту где ключ - тип ошибки а значение - список задач с таким типом ошибки
     */
    public static Map<ProgramErrorType, List<Operation>> findWrongOperation(FlyProgram flyProgram) {
        Map<ProgramErrorType, List<Operation>> wrongOperations = new EnumMap<>(ProgramErrorType.class);

        if (flyProgram == null || CollectionUtils.isEmpty(flyProgram.getOperations())) {
            wrongOperations.put(ProgramErrorType.IS_EMPTY, Collections.emptyList());
            return wrongOperations;
        }

        for (Operation operation : flyProgram.getOperations()) {
            List<Operation> duplicate = flyProgram.getOperations()
                    .stream()
                    .filter(o -> !o.equals(operation) &&
                            o.getVariable().equals(operation.getVariable()) &&
                            o.getDeltaT().equals(operation.getDeltaT()))
                    .collect(toList());

            addOperationsIfNeed(wrongOperations, duplicate, ProgramErrorType.DUPLICATE);

            List<Operation> notUniqueId = flyProgram.getOperations()
                    .stream()
                    .filter(o -> !o.equals(operation)
                            && o.getId().equals(operation.getId()))
                    .collect(toList());

            addOperationsIfNeed(wrongOperations, notUniqueId, ProgramErrorType.NOT_UNIQUE_ID);

            if (!OperationValidator.isValid(operation)) {
                addOperationsIfNeed(wrongOperations, Arrays.asList(operation), ProgramErrorType.WRONG_VALUES);
            }
        }

        return wrongOperations;
    }

    private static void addOperationsIfNeed(Map<ProgramErrorType, List<Operation>> wrongOperations,
                                            List<Operation> operations,
                                            ProgramErrorType type) {
        if (!CollectionUtils.isEmpty(operations)) {
            wrongOperations.merge(type, operations, mergeLists());
        }
    }

    private static BiFunction<List<Operation>, List<Operation>, List<Operation>> mergeLists() {
        return (oldValue, added) -> {
            oldValue.addAll(added);
            return oldValue;
        };
    }
}
