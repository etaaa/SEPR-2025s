import {Component, OnInit} from '@angular/core';
import {RouterLink} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Owner} from 'src/app/dto/owner';
import {OwnerService} from 'src/app/service/owner.service';

@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  imports: [
    RouterLink
  ],
  standalone: true,
  styleUrls: ['./owner.component.scss']
})
export class OwnerComponent implements OnInit {
  owners: Owner[] = [];
  bannerError: string | null = null;

  constructor(
    private service: OwnerService,
    private notification: ToastrService,
  ) {
  }

  ngOnInit(): void {
    this.reloadOwners();
  }

  reloadOwners() {
    this.service.getAll()
      .subscribe({
        next: data => {
          this.owners = data;
          this.bannerError = null;
        },
        error: error => {
          console.error('Error fetching owners', error);
          this.bannerError = 'Could not fetch owners: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Owners');
        }
      });
  }
}
