package com.kdt.nbe151team08.menu.service;

import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.menu.dto.MenuRequest;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import com.kdt.nbe151team08.menu.entity.Menu;
import com.kdt.nbe151team08.menu.repository.MenuRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<MenuResponse> getMenus(){
        return menuRepository.findAll(Sort.by("menuId"))  // 명세서상 메뉴 아이디 정렬
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    public MenuResponse getMenu(int id) {
        return MenuResponse.from(findMenu(id));
    }

    private Menu findMenu(int id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("MENU_NOT_FOUND","해당하는 메뉴가 없습니다."));
    }

    @Transactional
    public MenuResponse createMenu(MenuRequest request){
        Menu menu = new Menu(
                request.categoryId(),
                request.name(),
                request.price(),
                request.description()
        );

        Menu save = menuRepository.save(menu);
        return MenuResponse.from(save);
    }

    @Transactional
    public MenuResponse updateMenu(int id, MenuRequest request){
        Menu originMenu = findMenu(id);
        originMenu.update(
                request.categoryId(),
                request.name(),
                request.price(),
                request.description()
        );

        return MenuResponse.from(originMenu);
    }

    public void deleteMenu(int id){
        Menu menu = findMenu(id);
        menuRepository.delete(menu);
    }

    @Transactional
    public MenuResponse changeAvailability(int id, Boolean available){
        Menu originMenu = findMenu(id);
        originMenu.isAvailability(available);

        return MenuResponse.from(originMenu);
    }

    public List<MenuResponse> searchMenus(String keyword) {
        if (keyword == null || keyword.isBlank()
                || keyword.strip().length() > 100) {
            throw new IllegalArgumentException("검색어는 1~100자여야 합니다.");
        }

        return menuRepository.searchByKeyword(keyword)
                .stream().map(MenuResponse::from).toList();
    }

    public List<MenuResponse> getAvailableMenus() {
        return menuRepository.findByAvailableTrueOrderByMenuIdAsc()
                .stream().map(MenuResponse::from).toList();
    }
}
