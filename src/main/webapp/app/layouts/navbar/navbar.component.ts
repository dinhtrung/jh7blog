import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SessionStorageService } from 'ngx-webstorage';

import { VERSION } from 'app/app.constants';
import { LANGUAGES } from 'app/config/language.constants';
import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { LayoutService } from '../layout.service';

@Component({
  selector: 'jhi-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  // isNavbarCollapsed = true;
  languages = LANGUAGES;
  version = '';
  account: Account | null = null;

  constructor(
    private layoutService: LayoutService, // + sidebar
    private loginService: LoginService,
    private translateService: TranslateService,
    private sessionStorageService: SessionStorageService,
    private accountService: AccountService,
    private router: Router
  ) {
    if (VERSION) {
      this.version = VERSION.toLowerCase().startsWith('v') ? VERSION : 'v' + VERSION;
    }
  }

  ngOnInit(): void {
    this.accountService.getAuthenticationState().subscribe(account => (this.account = account));
  }

  changeLanguage(languageKey: string): void {
    this.sessionStorageService.store('locale', languageKey);
    this.translateService.use(languageKey);
  }

  collapseNavbar(): void {
    this.layoutService.isNavbarCollapsed = true;
  }

  isNavbarCollapsed(): boolean {
    return this.layoutService.isNavbarCollapsed;
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.collapseNavbar();
    this.loginService.logout();
    this.router.navigate(['']);
  }

  toggleNavbar(): void {
    this.layoutService.toggleNavbar();
  }

  // + sidebar
  toggleSidebarPin(): void {
    this.layoutService.toggleSidebarPin();
  }
  toggleSidebar(): void {
    this.layoutService.toggleSidebar();
  }
}
