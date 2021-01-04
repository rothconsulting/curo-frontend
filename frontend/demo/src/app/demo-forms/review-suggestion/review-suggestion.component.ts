import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-review-suggestion',
  templateUrl: './review-suggestion.component.html',
  styleUrls: ['./review-suggestion.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewSuggestionComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
