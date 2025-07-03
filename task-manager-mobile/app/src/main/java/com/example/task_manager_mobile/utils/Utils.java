package com.example.task_manager_mobile.utils;

import com.example.task_manager_mobile.enums.TaskStatus;

public class Utils {
    public static String generateStatusTextFromStatus(TaskStatus taskStatus) {
        switch (taskStatus) {
            case DONE:
                return "Finalizada";
            case IN_PROGRESS:
                return "Em andamento";
            case TO_DO:
                return "Pendente";
            default:
                return "Indefinido";
        }
    }
}
