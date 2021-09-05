import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LayoutService {
  isSidebarPinned = false;
  isSidebarToggeled = false;
  isNavbarCollapsed = false;

  toggleSidebar(): void {
    this.isSidebarToggeled = !this.isSidebarToggeled;
  }

  toggleSidebarPin(): void {
    this.isSidebarPinned = !this.isSidebarPinned;
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed = true;
  }

  toggleNavbar(): void {
    this.isNavbarCollapsed = !this.isNavbarCollapsed;
  }

  getSidebarStat(): any {
    return {
      isSidebarPinned: this.isSidebarPinned,
      isSidebarToggeled: this.isSidebarToggeled,
    };
  }
}
