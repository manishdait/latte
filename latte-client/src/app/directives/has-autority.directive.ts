import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { Authority } from '../model/role.type';
import { AuthService } from '../service/auth.service';

@Directive({
  selector: '[hasAuthority],[hasAnyAuthority]'
})
export class HasAuthorityDirective {
  constructor(private templateRef: TemplateRef<unknown>, private viewContainer: ViewContainerRef, private authService: AuthService) {}

  @Input()
  set hasAuthority(authorities: Authority[]) {
    const userAuthorities: Authority[] = this.authService.user.role.authorities;

    for(let authority of authorities) {
      if (!userAuthorities.includes(authority)) {
        this.viewContainer.clear();
        return;
      }
    }

    this.viewContainer.createEmbeddedView(this.templateRef);
  }

  @Input()
  set hasAnyAuthority(authorities: Authority[]) {
    const userAuthorities: Authority[] = this.authService.user.role.authorities;

    for(let authority of authorities) {
      if (userAuthorities.includes(authority)) {
        this.viewContainer.createEmbeddedView(this.templateRef);
        return;
      }
    }
    
    this.viewContainer.clear();
  }
}
