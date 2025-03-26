import {Component, OnInit, OnDestroy} from '@angular/core';
import {CommonModule, Location} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse} from 'src/app/dto/horse';
import {ConfirmDeleteDialogComponent} from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import {environment} from 'src/environments/environment';
import {Subscription} from 'rxjs';
import {ErrorFormatterService} from "../../../service/error-formatter.service";

const baseUri = environment.backendUrl;

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  imports: [
    CommonModule,
    RouterLink,
    ConfirmDeleteDialogComponent
  ],
  standalone: true,
  styleUrls: ['./horse-detail.component.scss']
})

/**
 * Component for displaying and managing the details of a single horse.
 */
export class HorseDetailComponent implements OnInit, OnDestroy {
  horse: Horse | null = null;
  loading = true;
  error = false;
  private routeSubscription: Subscription | null = null;

  constructor(
    private service: HorseService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private location: Location
  ) {
  }

  /**
   * Initializes the component by subscribing to route parameters and loading horse details.
   */
  ngOnInit(): void {
    this.routeSubscription = this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loading = true;
        this.error = false;
        this.loadHorse(+id);
      } else {
        this.error = true;
        this.loading = false;
      }
    });
  }

  /**
   * Cleans up the component by unsubscribing from route parameter subscription.
   */
  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  /**
   * Loads the horse details for the given ID.
   *
   * @param id The ID of the horse to load
   */
  loadHorse(id: number): void {
    this.service.getById(id).subscribe({
      next: data => {
        this.horse = data;
        this.loading = false;
      },
      error: error => {
        console.error('Error loading horse details', error);
        this.notification.error(this.errorFormatter.format(error), 'Could Not Load Horse Details', {
          enableHtml: true,
          timeOut: 10000,
        });
        this.loading = false;
        this.error = true;
      }
    });
  }

  /**
   * Deletes the currently loaded horse and navigates back to the horse list.
   */
  deleteHorse(): void {
    if (this.horse) {
      this.service.deleteHorse(this.horse).subscribe({
        next: () => {
          this.router.navigate(['/horses']);
        },
        error: () => {
          // Error handling is already done in the service
        }
      });
    }
  }

  /**
   * Constructs the full URL for the horse's image, if available.
   *
   * @returns The full image URL or null if no image exists
   */
  getImageUrl(): string | null {
    if (this.horse?.imageUrl) {
      return baseUri + this.horse.imageUrl;
    }
    return null;
  }

  /**
   * Formats a date object as a localized date string.
   *
   * @param date The date to format
   * @returns The formatted date string or an empty string if date is undefined
   */
  formatDate(date: Date | undefined): string {
    return date ? new Date(date).toLocaleDateString() : '';
  }

  /**
   * Converts the horse's sex value to a display-friendly string.
   *
   * @param sex The sex value of the horse (e.g., 'FEMALE', 'MALE')
   * @returns The display string ('Female' or 'Male')
   */
  getSexDisplay(sex: string | undefined): string {
    return sex === 'FEMALE' ? 'Female' : 'Male';
  }

  /**
   * Constructs the full name of the horse's owner.
   *
   * @returns The owner's full name or 'None' if no owner exists
   */
  getOwnerFullName(): string {
    if (!this.horse?.owner) {
      return 'None';
    }
    return `${this.horse.owner.firstName} ${this.horse.owner.lastName}`;
  }

  /**
   * Navigates back to the previous page in the browser history.
   */
  public goBack(): void {
    this.location.back();
  }
}
