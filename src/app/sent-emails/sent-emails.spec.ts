import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentEmailsComponent } from './sent-emails';

describe('SentEmails', () => {
  let component: SentEmailsComponent;
  let fixture: ComponentFixture<SentEmailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SentEmailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentEmailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
