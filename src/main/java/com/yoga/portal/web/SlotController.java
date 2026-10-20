package com.yoga.portal.web;

import com.yoga.portal.repository.YogaSlotRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SlotController {

    private final YogaSlotRepository slots;

    public SlotController(YogaSlotRepository slots) {
        this.slots = slots;
    }

    @GetMapping("/slots")
    @Transactional(readOnly = true)
    public String slots(Model model) {
        model.addAttribute("slots", slots.findAllByOrderByStartTimeAsc());
        return "slots";
    }
}
