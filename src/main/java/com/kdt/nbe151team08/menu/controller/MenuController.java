package com.kdt.nbe151team08.menu.controller;

import com.kdt.nbe151team08.menu.dto.MenuAvailabilityRequest;
import com.kdt.nbe151team08.menu.dto.MenuPageResponse;
import com.kdt.nbe151team08.menu.dto.MenuRequest;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import com.kdt.nbe151team08.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500"
})
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuResponse> getMenus(){
        return menuService.getMenus();
    }

    @GetMapping("/{menuId}")
    public MenuResponse getMenu(@PathVariable("menuId") int id){
        return menuService.getMenu(id);
    }

    @GetMapping("/available")
    public List<MenuResponse> getAvailable(){
        return menuService.getAvailableMenus();
    }

    @GetMapping("/search")
    public List<MenuResponse> searchMenus(@RequestParam(required = false) String keyword){
        return menuService.searchMenus(keyword);
    }

    @GetMapping("/page")
    public MenuPageResponse getMenuPages(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size){
        return menuService.getMenuPages(page,size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse createMenu(@Valid @RequestBody MenuRequest request){
        return menuService.createMenu(request);
    }

    @PutMapping("{menuId}")
    public MenuResponse updateMenu(@PathVariable("menuId") int id,@Valid @RequestBody MenuRequest request ){
        return menuService.updateMenu(id, request);
    }

    @DeleteMapping("/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(@PathVariable("menuId") int id){
        menuService.deleteMenu(id);
    }


    @PutMapping("{menuId}/availability")
    public MenuResponse updateAvailability(@PathVariable("menuId") int id,@Valid @RequestBody MenuAvailabilityRequest request ){
        return menuService.changeAvailability(id, request.available());
    }


}
