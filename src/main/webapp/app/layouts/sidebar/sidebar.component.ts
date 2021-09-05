import { Component, OnInit } from '@angular/core';
import { LayoutService } from '../layout.service';
import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { ProfileService } from 'app/layouts/profiles/profile.service';
@Component({
  selector: 'jhi-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit {
  inProduction?: boolean;
  openAPIEnabled?: boolean;
  account: Account | null = null;

  constructor(private accountService: AccountService, private profileService: ProfileService, private layoutService: LayoutService) {}

  ngOnInit(): void {
    // try to log in automatically
    this.accountService.getAuthenticationState().subscribe(account => (this.account = account));

    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction = profileInfo.inProduction;
      this.openAPIEnabled = profileInfo.openAPIEnabled;
    });
  }

  toggleSidebar(): void {
    this.layoutService.toggleSidebar();
  }
  collapseNavbar(): void {
    this.layoutService.collapseNavbar();
  }
}
