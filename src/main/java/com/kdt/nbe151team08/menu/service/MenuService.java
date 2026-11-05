package com.kdt.nbe151team08.menu.service;

import com.kdt.nbe151team08.common.exception.ConflictException;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.menu.dto.MenuPageResponse;
import com.kdt.nbe151team08.menu.dto.MenuRequest;
import com.kdt.nbe151team08.menu.dto.MenuResponse;
import com.kdt.nbe151team08.menu.entity.Menu;
import com.kdt.nbe151team08.menu.repository.MenuRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /**
     * 전체 메뉴 목록 — 조회가 잦아 Redis(menus/'all')에 캐시함.
     * TTL: application.yaml spring.cache.redis.time-to-live(60s).
     */
    @Cacheable(value = "menus", key = "'all'")
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
                .orElseThrow(() -> new NotFoundException("MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다."));
    }

    // 메뉴 변경 시 목록 캐시 제거. 다음 getMenus()는 DB를 다시 읽음
    // [A19 캐시 추가] key = "'all'" → allEntries = true : A06(all)뿐 아니라 A19(available) 캐시도 함께 지우기 위해 변경 (아래 4개 메서드 공통)
    @Transactional
    @CacheEvict(cacheNames = "menus", allEntries = true)
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
    @CacheEvict(cacheNames = "menus", allEntries = true)
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

    @Transactional
    @CacheEvict(cacheNames = "menus", allEntries = true)
    public void deleteMenu(int id){
        Menu menu = findMenu(id);
        if (menuRepository.existsInOrderItemByMenuId(id) == 1L){
            throw new ConflictException("MENU_IN_USE", "주문에 사용된 메뉴는 삭제할 수 없습니다.");
        }

        menuRepository.delete(menu);
    }

    @Transactional
    @CacheEvict(cacheNames = "menus", allEntries = true)
    public MenuResponse changeAvailability(int id, boolean available){
        Menu originMenu = findMenu(id);
        originMenu.isAvailability(available);

        return MenuResponse.from(originMenu);
    }

    public List<MenuResponse> searchMenus(String keyword) {
        if (keyword == null || keyword.isBlank()
                || keyword.strip().length() > 100) {
            throw new ValidationException("keyword", "검색어는 1~100자여야 합니다.");
        }

        return menuRepository.searchByKeyword(keyword)
                .stream().map(MenuResponse::from).toList();
    }

    // [A19 캐시 추가] 판매 중 메뉴 목록을 Redis(menus::available)에 저장, 메뉴 변경 시 @CacheEvict(allEntries)로 함께 지움
    @Cacheable(value = "menus", key = "'available'")
    public List<MenuResponse> getAvailableMenus() {
        return menuRepository.findByAvailableTrueOrderByMenuIdAsc()
                .stream().map(MenuResponse::from).toList();
    }


    @Transactional(readOnly = true)
    public MenuPageResponse getMenuPages(int page, int size){
        if (page < 0) {
            throw new ValidationException("page","page는 0 이상이어야 합니다.");
        }

        if (size < 1 || size > 50) {
            throw new ValidationException("size","size는 1~50이어야 합니다.");
        }

        Pageable fixed = PageRequest.of(page, size, Sort.by("menuId").ascending());
        Page<Menu> found = menuRepository.findAll(fixed);
        Page<MenuResponse> responses = found.map(MenuResponse::from);

        return new MenuPageResponse(
                responses.getContent(),
                responses.getNumber(),
                responses.getSize(),
                responses.getTotalElements(),
                responses.getTotalPages()
        );
    }
}
