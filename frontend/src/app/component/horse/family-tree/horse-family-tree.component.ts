import {Component, OnInit, OnDestroy} from '@angular/core';
import {CommonModule, Location} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpParams} from '@angular/common/http';
import {ToastrService} from 'ngx-toastr';
import {environment} from 'src/environments/environment';
import {Horse} from 'src/app/dto/horse';
import {HorseService} from 'src/app/service/horse.service';
import {ConfirmDeleteDialogComponent} from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import {Subscription} from 'rxjs';
import {ErrorFormatterService} from "../../../service/error-formatter.service";

interface FamilyTreeHorse extends Horse {
  isExpanded?: boolean;
  mother?: FamilyTreeHorse;
  father?: FamilyTreeHorse;
}

@Component({
  selector: 'app-horse-family-tree',
  templateUrl: './horse-family-tree.component.html',
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    ConfirmDeleteDialogComponent
  ],
  standalone: true,
  styleUrls: ['./horse-family-tree.component.scss']
})
export class HorseFamilyTreeComponent implements OnInit, OnDestroy {
  horse: FamilyTreeHorse | null = null;
  loading = true;
  error = false;
  generations = 3;
  horseForDeletion: Horse | undefined;
  private routeSubscription: Subscription | null = null;
  private baseUri = environment.backendUrl;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private notification: ToastrService,
    private service: HorseService,
    private errorFormatter: ErrorFormatterService,
    private location: Location
  ) {
  }

  ngOnInit(): void {
    this.routeSubscription = this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.route.queryParamMap.subscribe(queryParams => {
          const gen = queryParams.get('generations');
          if (gen) {
            this.generations = +gen;
          }
          this.loadFamilyTree(+id, this.generations);
        });
      } else {
        this.error = true;
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  public onCancel(): void {
    this.location.back();
  }

  loadFamilyTree(id: number, generations: number): void {
    this.loading = true;
    this.error = false;

    let params = new HttpParams()
      .set('generations', generations.toString());

    this.http.get<FamilyTreeHorse>(`${this.baseUri}/horses/${id}/familytree`, {params})
      .subscribe({
        next: (data) => {
          this.horse = this.processHorseData(data);
          this.loading = false;
        },
        error: error => {
          console.error('Error loading family tree', error);
          this.notification.error(this.errorFormatter.format(error), 'Could not load family tree', {
            enableHtml: true,
            timeOut: 10000,
          });
          this.loading = false;
          this.error = true;
        }
      });
  }

  private processHorseData(horse: FamilyTreeHorse): FamilyTreeHorse {
    if (!horse) return horse;
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    horse.isExpanded = true;
    if (horse.mother) {
      horse.mother = this.processHorseData(horse.mother);
    }
    if (horse.father) {
      horse.father = this.processHorseData(horse.father);
    }

    return horse;
  }

  toggleExpand(horse: FamilyTreeHorse): void {
    horse.isExpanded = !horse.isExpanded;
  }

  updateGenerations(): void {
    if (this.generations > 25) {
      this.notification.error('Maximum generations allowed is 25', 'Invalid Input');
      return;
    }

    if (this.horse && this.horse.id) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {generations: this.generations},
        queryParamsHandling: 'merge'
      }).then(() => {
        if (this.horse && this.horse.id !== undefined) {
          this.loadFamilyTree(this.horse.id, this.generations);
        } else {
          console.error('Cannot load family tree: Horse ID is missing');
          this.notification.error('Cannot load family tree', 'Missing horse ID');
        }
      });
    }
  }

  public goBack(): void {
    this.location.back();
  }

  validateGenerations(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = parseInt(input.value, 10);
    if (value > 25) {
      input.value = '25';
      this.generations = 25;
      this.notification.error('Maximum generations allowed is 25', 'Invalid Input');
    }
  }

  formatDate(date: Date): string {
    return date.toLocaleDateString();
  }

  deleteHorse(horse: Horse): void {
    this.service.deleteHorse(horse).subscribe({
      next: () => {
        if (horse.id === this.horse?.id) {
          this.router.navigate(['/horses']);
        } else {
          if (this.horse && this.horse.id !== undefined) {
            this.loadFamilyTree(this.horse.id, this.generations);
          } else {
            console.error('Cannot load family tree: Horse ID is missing');
            this.notification.error('Cannot load family tree', 'Missing horse ID');
          }
        }
      },
      error: () => {
        // Error handling is already done in the service
      }
    });
  }
}
