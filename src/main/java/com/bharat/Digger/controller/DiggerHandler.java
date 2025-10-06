package com.bharat.Digger.controller;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@ControllerAdvice
public class DiggerHandler {
    @ExceptionHandler(exception = IOException.class)
    public ModelAndView handleIOException(IOException e) {
        var mvc = new ModelAndView("Error");
        mvc.addObject("message", e.getMessage());
        return mvc;
    }
}
