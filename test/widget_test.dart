import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:koder/app.dart';

void main() {
  testWidgets('Koder boots with bottom navigation', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: KoderApp(),
      ),
    );

    expect(find.text('Editor'), findsOneWidget);
    expect(find.text('Files'), findsOneWidget);
    expect(find.text('Settings'), findsOneWidget);
  });
}
