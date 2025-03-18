import {Routes} from '@angular/router';
import {
  HorseCreateEditComponent,
  HorseCreateEditMode
} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {HorseDetailComponent} from "./component/horse/horse-detail/horse-detail.component";


export const routes: Routes = [
  {
    path: 'horses', children: [
      {path: '', component: HorseComponent},
      {path: 'create', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.create}},
      {path: ':id/detail', component: HorseDetailComponent},
      {path: ':id/edit', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.edit}}
    ]
  },
  {path: '**', redirectTo: 'horses'},
];
